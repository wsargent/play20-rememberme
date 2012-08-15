package authentication

import play.api.mvc._
import play.api.Logger
import util.Random

trait ActionHandler extends SessionSaver {

  def authenticationService: AuthenticationService

  def userService : UserInfoService

  def logger: Logger

  def gotoSuspiciousAuthDetected[A](request: Request[A]): Result

  def actionHandler[A](action: Action[A]): Action[A] = {
    Action(action.parser) {
      rawRequest =>
        if (requiresContext(rawRequest)) {
          actionWithContext(rawRequest) {
            request => action(request)
          }
        } else {
          logger.trace("actionHandler: no context required for " + rawRequest)
          action(rawRequest)
        }
    }
  }

  /**
   * Enhances the action with a request object that can contain important context information.
   */
  def actionWithContext[A](rawRequest: Request[A])(action: Request[A] => Result): Result = {
    logger.trace("actionWithContext: request = " + rawRequest)

    // Look for session credentials first, then cookie.
    withSessionCredentials(rawRequest).map {
      request =>
        action(request)
    }.map {
      logger.debug("actionWithContext: returning with session credentials " + rawRequest)
      return _
    }

    withRememberMeCookie(rawRequest).map {
      rememberMe =>
        for {
          userId <- rememberMe.userId
          series <- rememberMe.series
          token <- rememberMe.token
        } yield {
          val result = authenticationService.authenticateWithCookie(userId, series, token).fold(
            fault => actionRejectingAuthentication(rawRequest) {
              request =>
                fault match {
                  case suspiciousFault:InvalidSessionCookieFault => {
                    logger.warn("actionWithContext: suspicious fault = " + suspiciousFault)
                    gotoSuspiciousAuthDetected(request)
                  }
                  case e:AuthenticationFault => {
                    logger.trace("actionWithContext: fault = " + e)
                    action(request)
                  }
                }
            },
            event => actionWithAuthentication(rawRequest, event) {
              request =>
                action(request)
            }
          )
          logger.debug("actionWithContext: returning with token credentials " + rawRequest)
          return result
        }
    }

    // Return the default.
    logger.debug("actionWithContext: returning with no credentials " + rawRequest)
    action(Context(rawRequest, None))
  }

  /**
   * Returns a context, using the underlying session id.
   *
   * @param rawRequest
   * @tparam A
   * @return
   */
  def withSessionCredentials[A](rawRequest: Request[A]): Option[Context[A]] = {
    for {
      sessionId <- rawRequest.session.get("sessionId")
      userInfo <- restoreFromSession(sessionId)
    } yield Context(rawRequest, Some(userInfo))
  }

  /**
   * Restores the user using the session id.
   *
   * @param sessionId
   * @return
   */
  def restoreFromSession(sessionId: String): Option[UserInfo] = {
    for {
      userId <- sessionStore.lookup(sessionId)
      userInfo <- userService.lookup(userId)
    } yield userInfo
  }

  /**
   * The remember me cookie was not valid for some reason.  Run through the action without any authed user.
   *
   * @param request
   * @param action
   * @tparam A
   * @return
   */
  def actionRejectingAuthentication[A](request: Request[A])(action: Request[A] => Result): Result = {
    val context = Context(request, None)
    val result = action(context)
    result match {
      case plainResult: PlainResult => {
        logger.debug("discarding remember me cookie")
        plainResult.discardingCookies(RememberMe.COOKIE_NAME)
      }
      case _ => result
    }
  }

  /**
   * Called when an action has successfully passed through authentication.  It saves the authenticated user id into the
   * session store, creates a Context object, and
   *
   * @param rawRequest the unwrapped request.
   * @param event the token event that was passed in.
   * @param action the action that we're composing on and will pass this info to.
   * @tparam A the type of request
   * @return the result of the action composition.
   */
  def actionWithAuthentication[A](rawRequest: Request[A], event: UserAuthenticatedWithTokenEvent)(action: Request[A] => Result): Result = {

    // Defer the action until we have the authentication saved off...
    val userId = event.userId
    val sessionId = saveAuthentication(userId)(rawRequest)
    val sessionCookie = SessionCookie("sessionId", sessionId)(rawRequest)

    // Create a new token on every cookie authentication, even if the series is the same.
    val rememberMe = RememberMe(userId, event.series, event.token)
    val rememberMeCookie = RememberMe.encodeAsCookie(rememberMe)

    // Look up and save off the user information for the rest of the action...
    val userInfo = userService.lookup(userId)
    val richRequest = Context(rawRequest, userInfo)
    val result = action(richRequest)
    result match {
      case plainResult: PlainResult => {
        logger.debug("Creating new remember me cookie " + rememberMe)
        plainResult.withCookies(sessionCookie, rememberMeCookie)
      }
      case _ => result
    }
  }

  /**
   * @param req the request header.
   * @return true if this requires a context instead of a raw request, false otherwise.
   */
  def requiresContext(implicit req: RequestHeader): Boolean = {
    !req.path.startsWith("/assets")
  }

  def withRememberMeCookie(implicit req: RequestHeader): Option[RememberMe] = {
    val cookie = req.cookies.get(RememberMe.COOKIE_NAME)
    val rememberMe = RememberMe.decodeFromCookie(cookie)
    // Cookie can be empty even if it exists :-(
    if (!rememberMe.isEmpty) Option(rememberMe) else None
  }

}