package controllers

import views.html
import models.{Password, User}
import play.api.mvc.{RequestHeader, Controller}
import play.api.data.Form
import play.api.data.Forms._
import play.api.Logger

import com.tersesystems.authentication._
import security.MySessionStore

/**
 * A basic sign up controller.  We use a weak password constraint here for ease
 * of use, but you are encouraged to install "passwdqc" and"strongPassword" instead.
 * See the BaseConstraints class for more details.
 *
 * @author wsargent
 */
object SignupController extends Controller
  with BaseConstraints
  with BaseActions
{
  def sessionStore = MySessionStore

  val logger = Logger(this.getClass)

  val signupForm = Form(
    mapping(
      "email" -> email,
      "fullName" -> text,
      "password" -> weakPassword
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
      try {
        signupForm.bindFromRequest.fold(
          err => {
            logger.debug("err = " + err)
            BadRequest(html.signup.index(err))
          },
          data => {
            val password = Password.parse(data.password)
            val user = User.register(data.email, data.fullName, password)
            gotoSignupSucceeded(user.email)
          }
        )
      } catch {
        case e: Exception => {
          logger.error("error = ", e)
          val errorMessage = "Internal error, could not register"
          Redirect(routes.SignupController.signup()) flashing (FLASH_ERROR -> errorMessage)
        }
      }
  }

  def gotoSignupSucceeded[A](userId: String)(implicit req: RequestHeader) = {
    logger.debug("gotoSignupSucceeded")
    Redirect(routes.SignupController.signupSuccess())
  }

}
