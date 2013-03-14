package controllers

import views.html
import models.User
import play.api.mvc.{Flash, RequestHeader, Controller}
import play.api.data.Form
import play.api.data.Forms._
import play.api.Logger

import com.tersesystems.authentication._
import security.MySessionStore

/**
 *
 * @author wsargent
 */
object SignupController extends Controller with SessionSaver[String] with BaseActions
{
  def sessionStore = MySessionStore

  private val logger = Logger(this.getClass)

  val signupForm = Form(
    mapping(
      "email" -> email,
      "fullName" -> text,
      "password" -> text(minLength = 8)
    )(SignupData.apply)(_ => None)
  )

  case class SignupData(email: String, fullName: String, password: String)

  def signup = Open {
    implicit ctx =>
      Ok(html.signup.index(signupForm))
  }

  def signupSuccess = Open {
    implicit ctx =>
      Ok(html.signup.success())
  }

  def signupPost = Open {
    implicit ctx =>
      logger.debug("signupPost:")
      signupForm.bindFromRequest.fold(
        err => {
          logger.error("err = " + err)
          BadRequest(html.signup.index(err))
        },
        data => {
          try {
            val user = User.register(data.email, data.fullName, data.password)
            gotoSignupSucceeded(user.email)
          } catch {
            case e: Exception => {
              logger.error("error = ", e)
              val errorMessage = "Internal error, could not register"
              Redirect(routes.SignupController.signup()) flashing (FLASH_ERROR -> errorMessage)
            }
          }
        }
      )
  }

  def gotoConfirmSucceeded[A](userId: String)(implicit req: RequestHeader) = {
    val sessionId = saveAuthentication(userId)
    val flash = Flash(Map(FLASH_SUCCESS -> "You have been confirmed."))
    Redirect(routes.Application.index()) withCookies SessionCookie("sessionId", sessionId) flashing (flash)
  }

  def gotoSignupSucceeded[A](userId: String)(implicit req: RequestHeader) = {
    logger.debug("gotoSignupSucceeded")
    Redirect(routes.SignupController.signupSuccess())
  }

}
