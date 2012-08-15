package controllers

import authentication._

import play.api.mvc._
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._

import models._

import views._
import authentication.UserAuthenticatedEvent
import scala.Some
import play.api.mvc.Cookie

object AuthController extends Controller with SessionSaver with BaseActions {

  val FLASH_INFO = "info"
  val FLASH_ERROR = "error"
  val FLASH_SUCCESS = "success"

  def logger = Logger(this.getClass)

  def userInfoService = User

  def sessionStore = SessionCache

  def authenticationService = BasicAuthenticationService

    val signupForm = Form(
      mapping(
        "email" -> email,
        "fullName" -> text,
        "password" -> text(minLength = 4)
      )(SignupData.apply)(_ => None)
    )

  case class SignupData(email: String, fullName:String, password: String)

  val loginForm = Form(mapping(
    "email" -> email,
    "password" -> nonEmptyText,
    "rememberMe" -> boolean
  )(authenticateUser)(_ => None)
    .verifying("Invalid username or password", _.isDefined)
  )

  def login = Open {
    implicit ctx =>
      Ok(html.auth.login(loginForm))
  }

  def suspicious = Open {
    implicit ctx =>
      Ok(html.auth.suspicious())
  }

  def authenticate = Open {
    implicit ctx =>
      loginForm.bindFromRequest.fold(
        err => {
          logger.debug("Bad request: err = " + err)
          authorizationFailed(ctx)
        },
        eventOption => {
          eventOption.map { event =>
            gotoLoginSucceeded(event.userId, event.series, event.token)
          }.getOrElse {
            // No event found, something bad happened.
            logger.error("authenticate: could not log in")
            authorizationFailed(ctx)
          }
        }
      )
  }

  def logout = Open {
    implicit ctx =>
      gotoLogoutSucceeded(ctx)
  }

  def signup = Open {
    implicit ctx =>
      Ok(html.auth.signup(signupForm))
  }

  def signupSuccess = Open {
    implicit ctx =>
      Ok(html.auth.signupSuccess())
  }

  def signupPost = Open {
    implicit ctx =>
      logger.debug("signupPost:")
      signupForm.bindFromRequest.fold(
        err => {
          logger.error("err = " + err)
          BadRequest(html.auth.signup(err))
        },
        data => {
          userInfoService.register(data.email, data.fullName, data.password).fold(
            fault => {
              logger.error("error = " + fault)
              val form = signupForm
              BadRequest(html.auth.signup(form))
            },
            user => {
              gotoSignupSucceeded(user.email)
            }
          )
        }
      )
  }

  def logoutSucceeded(req: RequestHeader): PlainResult = {
    logger.debug("logoutSucceeded")
    Redirect(routes.Application.index())
  }

  def suspiciousActivity(implicit req: RequestHeader): PlainResult = {
    Redirect(routes.AuthController.suspicious())
  }

  def authenticationFailed(implicit req: RequestHeader): PlainResult = {
    logger.debug("authenticationFailed: " + req)
    Redirect(routes.Application.index()) withCookies SessionCookie("access_uri", req.uri)
  }

  def gotoLoginSucceeded[A](userId: UserID, series: Option[Long], token: Option[Long])(implicit req: RequestHeader) = {
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

  def gotoSignupSucceeded[A](userId: UserID)(implicit req: RequestHeader) = {
    logger.debug("gotoSignupSucceeded")
    Redirect(routes.AuthController.signupSuccess())
  }

  def gotoConfirmSucceeded[A](userId: UserID)(implicit req: RequestHeader) = {
    val sessionId = saveAuthentication(userId)
    val flash = Flash(Map(FLASH_SUCCESS -> "You have been confirmed."))
    Redirect(routes.Application.index()) withCookies SessionCookie("sessionId", sessionId) flashing (flash)
  }

  def gotoPasswordResetSucceeded[A](userId: UserID)(implicit req: RequestHeader) = {
    val sessionId = saveAuthentication(userId)
    val flash = Flash(Map(FLASH_SUCCESS -> "Your password has been reset."))
    Redirect(routes.Application.index()) withCookies SessionCookie("sessionId", sessionId) flashing (flash)
  }

  def gotoLogoutSucceeded(implicit req: RequestHeader) = {
    req.session.get("sessionId") foreach {
      sessionStore.deleteSession(_)
    }
    logoutSucceeded(req).withNewSession discardingCookies (RememberMe.COOKIE_NAME)
  }

  def loginSucceeded(req: RequestHeader): PlainResult = {
    val uri = req.session.get("access_uri").getOrElse(routes.Application.index().url)
    req.session - "access_uri"
    Redirect(uri)
  }

  def authorizationFailed(req: RequestHeader): PlainResult = {
    logger.debug("authorizationFailed")
    Redirect(routes.AuthController.login()) discardingCookies (RememberMe.COOKIE_NAME) flashing (FLASH_ERROR -> "Cannot login with username/password")
  }

  def authenticateUser(email: String, password: String, rememberMe: Boolean): Option[UserAuthenticatedEvent] = {
    authenticationService.authenticate(email, password, rememberMe).fold(
      fault => {
        logger.debug("authenticateUser: failed")
        None
      },
      event => Some(event)
    )
  }
}
