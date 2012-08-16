import play.api._
import play.api.mvc._

import security.MyActionHandler

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

}