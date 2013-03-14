package security

import com.tersesystems.authentication._
import play.api.Logger


import play.api.mvc.{BodyParser, Result, Request, Action}

import controllers.AuthController

import models.User

/**
 * An action handler with all the dependencies resolved.  This will authenticate a user
 * with the user id and user types resolved, and hook up the dependencies.
 *
 * This class hooks up to the BaseActions trait in the controller.
 */
object MyAuthenticationHandler extends AuthenticationHandler[String, User]
{
  val logger = Logger(this.getClass)

  val SESSION_ID = "sessionId"

  val authenticationService = MyAuthenticationService

  val userService = models.User

  val sessionStore = MySessionStore

  val contextConverter = new ContextConverter[User]
  {
    def apply[A](request: Request[A], user: Option[User]) = security.MyContext[A](request, user)
  }

  val userIdConverter = new UserIdConverter[String]
  {
    def apply(userId: String) = userId
  }

  def apply[A](bp: BodyParser[A])(f: Request[A] => Result): Action[A] = {
    actionHandler(bp)(f)
  }

  def gotoSuspiciousAuthDetected[A](request: Request[A]): Result = {
    AuthController.suspiciousActivity(request)
  }

}
