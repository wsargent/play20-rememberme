package test

import org.specs2.mutable._

import play.api.mvc.Cookie
import play.api.test._
import play.api.test.Helpers._

import models._
import security._

import com.tersesystems.authentication.RememberMe
import util.Random
import controllers.routes

/**
 * This class tests out the authentication module, primarily the AuthController to ensure that it does everything correctly
 */
class ApplicationSpec extends Specification
{
  def fakeApp = FakeApplication(additionalConfiguration = inMemoryDatabase("test"))

  //  # Authentication
  //  GET   /login                           controllers.AuthController.login
  //  POST  /login                           controllers.AuthController.authenticate
  //  GET   /logout                          controllers.AuthController.logout

  val SESSION_ID = MyAuthenticationHandler.SESSION_ID

  val discardCookie = Cookie(RememberMe.COOKIE_NAME, "", Some(0), "/", None, secure = false, httpOnly = true)

  "Login" should {
    val postLogin = FakeRequest(POST, "/login")
    val postLogout = FakeRequest(POST, "/logout")

    "accept login of existing user" in running(fakeApp) {
      val password = MyPasswordService.encryptPassword("password")
      val user = User(name = "fullName", email = "email@example.com", password = password)
      User.create(user)

      route(postLogin.withFormUrlEncodedBody(
        ("email" -> "email@example.com"),
        ("password" -> "password"))) must beSome.which {
        r =>
          session(r).get(SESSION_ID) must beSome
          cookies(r).get(RememberMe.COOKIE_NAME) must beNone
          flash(r).data must not haveKey (controllers.FLASH_ERROR)
          status(r) must equalTo(SEE_OTHER)
      }
    }

    "accept login and set cookie of existing user" in running(fakeApp) {
      val password = MyPasswordService.encryptPassword("password")
      val user = User.create(User(name = "fullName", email = "email@example.com", password = password))

      route(postLogin.withFormUrlEncodedBody(
        ("email" -> "email@example.com"),
        ("password" -> "password"),
        ("rememberMe" -> "true")
      )) must beSome.which {
        r =>
          session(r).get(SESSION_ID) must beSome

          val rememberMe = RememberMe.decodeFromCookie(cookies(r).get(RememberMe.COOKIE_NAME))
          rememberMe.userId must equalTo(Some(user.email))
          rememberMe.series must beSome
          rememberMe.token must beSome

          flash(r).data must not haveKey (controllers.FLASH_ERROR)
          status(r) must equalTo(SEE_OTHER)
      }
    }

    "reject login of non-existing user" in running(fakeApp) {
      route(postLogin.withFormUrlEncodedBody(
        ("email" -> "email@example.com"),
        ("password" -> "password"))) must beSome.which {
        r =>
          flash(r).data must haveKey(controllers.FLASH_ERROR)
          status(r) must equalTo(SEE_OTHER)
      }
    }

    "reject login of user with wrong password" in running(fakeApp) {
      val password = MyPasswordService.encryptPassword("password")
      val user = User.create(User(name = "fullName", email = "email@example.com", password = password))

      route(postLogin.withFormUrlEncodedBody(
        ("email" -> "email@example.com"),
        ("password" -> "invalidpassword"))) must beSome.which {
        r =>
          flash(r).data must haveKey(controllers.FLASH_ERROR)
          status(r) must equalTo(SEE_OTHER)
      }
    }

    "log out an existing user" in running(fakeApp) {
      route(postLogout.withFormUrlEncodedBody()) must beSome.which {
        r =>
          session(r).get(SESSION_ID) must beNone
          // Must have a discarding cookie
          cookies(r).get(RememberMe.COOKIE_NAME) must equalTo(Some(discardCookie))

          // Should go back to the index page.
          status(r) must equalTo(SEE_OTHER)
          redirectLocation(r) must beSome.which { _ must equalTo(routes.Application.index().url) }
      }
    }
  }

  "Index page" should {
    val getIndex = FakeRequest(GET, "/")

    "be able to see the index page" in running(fakeApp) {
      val result = route(getIndex)
      result must beSome.which {
        r =>
          session(r).get(SESSION_ID) must beNone
          status(r) must equalTo(OK)
      }
    }

    "be able to see the index page as logged in with cookie" in running(fakeApp) {
      // Create a user and a remember me cookie to make things work.
      val password = MyPasswordService.encryptPassword("password")
      val user = User.create(User(name = "fullName", email = "email@example.com", password = password))
      val t = RememberMeToken.create(RememberMeToken(user.email, Random.nextLong(), Random.nextLong()))

      val rememberMe = RememberMe(user.email, series = t.series, token = t.token)
      val rememberMeCookie = RememberMe.encodeAsCookie(rememberMe)
      val result = route(getIndex.withCookies(rememberMeCookie))
      result must beSome.which {
        r =>
          session(r).get(SESSION_ID) must beSome
          status(r) must equalTo(OK)
      }
    }

    "logout and flag a suspicious cookie" in running(fakeApp) {
      val password = MyPasswordService.encryptPassword("password")
      val user = User.create(User(name = "fullName", email = "email@example.com", password = password))

      // Create the token...
      val t = RememberMeToken.create(RememberMeToken(user.email, Random.nextLong(), Random.nextLong()))

      // Create a token that is NOT the same as the series.
      val rememberMe = RememberMe(user.email, series = t.series, token = Random.nextLong())
      val rememberMeCookie = RememberMe.encodeAsCookie(rememberMe)
      val result = route(getIndex.withCookies(rememberMeCookie))

      result must beSome.which {
        r =>
          session(r).get(SESSION_ID) must beNone
          // Must have a discarding cookie
          cookies(r).get(RememberMe.COOKIE_NAME) must equalTo(Some(discardCookie))
          status(r) must equalTo(SEE_OTHER)
          // It should land on the "suspicious" page...
          redirectLocation(r) must beSome.which { _ must equalTo(routes.AuthController.suspicious().url) }
      }
    }
  }

  "Asset Page" should {

    "not have a session" in running(fakeApp) {
      val result = route(FakeRequest(GET, "/assets/images/favicon.png"))
      result must beSome.which {
        r =>
          session(r).get(SESSION_ID) must beNone
          status(r) must equalTo(OK)
      }
    }

  }

}
