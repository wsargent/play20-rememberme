package authentication

import controllers.routes

import java.util.UUID
import play.api.data.Forms._
import play.api.data._
import play.api.mvc.Results._
import play.api.mvc._

import play.api.Logger

trait AuthImpl {

  val loginForm = Form(mapping(
    "email" -> email,
    "password" -> nonEmptyText,
    "rememberMe" -> boolean
  )(authenticateUser)(_ => None)
    .verifying("Invalid username or password", _.isDefined)
  )

  val FLASH_INFO = "info"
  val FLASH_ERROR = "error"
  val FLASH_SUCCESS = "success"

  def logger: Logger

  def userInfoService: UserInfoService

  def logoutSucceeded(req: RequestHeader): PlainResult =
    Redirect(routes.HomeController.index())

  def suspiciousActivity(implicit req: RequestHeader): PlainResult = {
    Redirect(routes.AuthController.suspicious())
  }

  def authenticationFailed(implicit req: RequestHeader): PlainResult = {
    logger.debug("authenticationFailed: " + req)
    Redirect(routes.HomeController.index()) withCookies SessionCookie("access_uri", req.uri)
  }

  def gotoLoginSucceeded[A](userId: UUID, series: Option[Long], token: Option[Long])(implicit req: RequestHeader) = {
    logger.debug("gotoLoginSucceeded: login succeeded, userId = " + userId)
    val sessionId = saveAuthentication(userId)
    val sessionCookie = SessionCookie("sessionId", sessionId)

    val rememberMeCookie = for {
      s <- series
      t <- token
    } yield {
      val rememberMe = RememberMe(userId, s, t)
      RememberMe.encodeAsCookie(rememberMe)
    }

    // Append a session cookie here.
    val cookies: Seq[Cookie] = rememberMeCookie.toList :+ sessionCookie
    loginSucceeded(req) withCookies (cookies: _*)
  }

  def gotoSignupSucceeded[A](userId: UUID)(implicit req: RequestHeader) = {
    logger.debug("gotoSignupSucceeded")
    Redirect(routes.AuthController.signupSuccess())
  }

  def gotoConfirmSucceeded[A](userId: UUID)(implicit req: RequestHeader) = {
    val sessionId = saveAuthentication(userId)
    val flash = Flash(Map(FLASH_SUCCESS -> "You have been confirmed."))
    Redirect(routes.HomeController.index()) withCookies SessionCookie("sessionId", sessionId) flashing (flash)
  }

  def gotoPasswordResetSucceeded[A](userId: UUID)(implicit req: RequestHeader) = {
    val sessionId = saveAuthentication(userId)
    val flash = Flash(Map(FLASH_SUCCESS -> "Your password has been reset."))
    Redirect(routes.HomeController.index()) withCookies SessionCookie("sessionId", sessionId) flashing (flash)
  }

  def gotoLogoutSucceeded(implicit req: RequestHeader) = {
    req.session.get("sessionId") foreach {
      sessionStore.deleteSession(_)
    }
    logoutSucceeded(req).withNewSession discardingCookies (RememberMe.COOKIE_NAME)
  }

  def loginSucceeded(req: RequestHeader): PlainResult = {
    val uri = req.session.get("access_uri").getOrElse(routes.HomeController.index().url)
    req.session - "access_uri"
    Redirect(uri)
  }

  def authorizationFailed(req: RequestHeader): PlainResult = {
    logger.debug("authorizationFailed")
    Redirect(routes.AuthController.login()) discardingCookies (RememberMe.COOKIE_NAME) flashing (FLASH_ERROR -> "Cannot login with username/password")
  }

  def authenticateUser(email: String, password: String, rememberMe: Boolean): Option[UserAuthenticatedEvent] = {
    userInfoService.authenticate(email, password, rememberMe).fold(
      fault => {
        logger.debug("authenticateUser: failed")
        None
      },
      event => Some(event)
    )
  }

}
