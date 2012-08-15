import controllers.AuthController
import models.{SessionCache, User, BasicAuthenticationService}
import play.api._
import play.api.mvc._

import authentication.ActionHandler

object Global extends GlobalSettings {

  /**
   * For every single request, find a matching handler.  If it's an Action[_], wrap it with the action handler first.
   *
   * @param request the raw request.
   * @return an optional handler.
   */
  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
    super.onRouteRequest(request).map {
      handler =>
        handler match {
          case a: Action[_] => MyActionHandler(a)
          case _ => handler
        }
    }
  }

  /**
   * An action handler with all the dependencies resolved.
   */
  object MyActionHandler extends ActionHandler {

    def logger = Logger(this.getClass)

    def authenticationService = BasicAuthenticationService

    def userService = User

    def sessionStore = SessionCache

    def apply[A](action: Action[A]): Action[A] = {
      actionHandler(action)
    }

    def gotoSuspiciousAuthDetected[A](request: Request[A]) : Result = {
      AuthController.suspiciousActivity(request)
    }
  }

}