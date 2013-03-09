package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import security.MyActionHandler

/**
 *
 * @author wsargent
 * @since 3/9/13
 */
class SignupControllerSpec extends Specification {

  def fakeApp = FakeApplication(additionalConfiguration = inMemoryDatabase("test"))

  val SESSION_ID = MyActionHandler.SESSION_ID

    //  # User signup
  //  GET   /signup                          controllers.AuthController.signup
  //  POST  /signup                          controllers.AuthController.signupPost
  //  GET   /signup/success                  controllers.AuthController.signupSuccess

  "Registration" should {

    "show an error when the user does not enter in correct details" in running(fakeApp) {
      val postSignup = FakeRequest(POST, "/signup")
      route(postSignup.withFormUrlEncodedBody()) must beSome.which {
        r =>
          status(r) must equalTo(BAD_REQUEST)
      }
    }

    "register a user correctly" in running(fakeApp) {
      val postSignup = FakeRequest(POST, "/signup")
      route(postSignup.withFormUrlEncodedBody(
        ("fullName", "First Last"),
        ("email", "email@example.com"),
        ("password", "password")
      )) must beSome.which {
        r =>
          status(r) must equalTo(SEE_OTHER)
      }
    }
  }

}
