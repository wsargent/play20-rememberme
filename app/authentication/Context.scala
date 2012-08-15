
package authentication

import play.api.mvc._

/**
 * This class extends WrappedRequest to associate authenticated user information with the request.
 *
 * @param request the raw request
 * @param me the authenticated user option.
 * @tparam A the request type.
 */
case class Context[A](request: Request[A], me: Option[UserInfo]) extends WrappedRequest(request) {

  def isAuth = me.isDefined

  def is(user: UserInfo) = me == Some(user)

  override def toString() = {
    "Context(" + method + " " + uri + " user=" + me + ")"
  }
}