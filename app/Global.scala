import models.{SessionCache, User, BasicAuthenticationService}
import play.api._
import play.api.mvc._

import authentication.ActionHandler
import play.mvc.Results.Redirect

object Global extends GlobalSettings {

  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
    super.onRouteRequest(request).map {
      handler =>
        handler match {
          case a: Action[_] => MyActionHandler(a)
          case _ => handler
        }
    }
  }

  object MyActionHandler extends ActionHandler {

    def authenticationService = BasicAuthenticationService

    def userService = User

    def sessionStore = SessionCache

    def apply[A](action: Action[A]): Action[A] = {
      actionHandler(action)
    }

    def gotoSuspiciousAuthDetected[A](request: Request[A]) = {
      controllers.Application.index()
    }
  }

}