package controllers

import play.api.mvc._
import com.tersesystems.authentication._
import security.MySessionStore

/**
 *
 * @author wsargent
 */
object ResetController extends Controller with SessionSaver[String] {

  def sessionStore = MySessionStore

  def gotoPasswordResetSucceeded[A](userId: String)(implicit req: RequestHeader) = {
    val sessionId = saveAuthentication(userId)
    val flash = Flash(Map(FlashNames.SUCCESS -> "Your password has been reset."))
    Redirect(routes.Application.index()) withCookies SessionCookie("sessionId", sessionId) flashing (flash)
  }

}
