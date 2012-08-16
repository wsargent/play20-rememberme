package security

import com.tersesystems.authentication._
import play.api.Logger


import play.api.mvc.{Result, Request, Action}

import controllers.AuthController

import models.User

/**
 * An action handler with all the dependencies resolved.
 */
object MyActionHandler extends ActionHandler[String, User] {

  lazy val logger = Logger(this.getClass)

  lazy val authenticationService = MyAuthenticationService

  lazy val userService = models.User

  lazy val sessionStore = MySessionStore

  lazy val contextConverter = new ContextConverter[User] {
    def apply[A](request: Request[A], user: Option[User]) = security.MyContext[A](request, user)
  }

  lazy val userIdConverter = new UserIdConverter[String] {
    def apply(userId: String) = userId
  }

  def apply[A](action: Action[A]): Action[A] = {
    actionHandler(action)
  }

  def gotoSuspiciousAuthDetected[A](request: Request[A]) : Result = {
    AuthController.suspiciousActivity(request)
  }

}
