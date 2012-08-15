package models

import authentication._
import play.api.db.DB
import anorm._
import scala.Left
import scala.Right
import scala.Left
import authentication.UserAuthenticatedEvent
import authentication.InvalidCredentialsFault
import authentication.UserAuthenticatedWithTokenEvent
import scala.Right
import util.Random
import play.api.Logger

/**
 *
 * @author wsargent
 * @since 8/14/12
 */

object BasicAuthenticationService extends AuthenticationService {

  private lazy val logger = Logger(this.getClass)

  /**
   * Authenticate a User.
   */
  def authenticate(email: String, password: String, rememberMe: Boolean): Either[AuthenticationFault, UserAuthenticatedEvent] = {
    val userOption = User.authenticate(email, password)

    userOption.map {
      user =>
        // When the user successfully logs in with Remember Me checked, a login cookie is issued in addition to the
        // standard session management cookie.[2]
        logger.debug("authenticate: right, rememberMe = " + rememberMe)
        val token = rememberMe match {
          case true => {
            val series = Random.nextLong()
            val token = Random.nextLong()
            val sessionToken = RememberMeToken(user.email, series, token)
            Option(RememberMeToken.create(sessionToken))
          }
          case false => None
        }
        logger.debug("authenticate: token = " + token)
        val event = UserAuthenticatedEvent(user.email, token.map(_.series), token.map(_.token))
        Right(event)
    }.getOrElse(Left(InvalidCredentialsFault()))
  }

  def authenticateWithCookie(userId: authentication.UserID, series: Long, token: Long): Either[AuthenticationFault, UserAuthenticatedWithTokenEvent] = {

    // When a non-logged-in user visits the site and presents a login cookie, the username, series, and token are
    // looked up in the database.
    RememberMeToken.findByUserIdAndSeries(userId, series).map {
      token =>
        // If the triplet is present, the user is considered authenticated.
        if (token.token == token) {
          // The used token is removed from the database.
          RememberMeToken.remove(token)
          // A new token is generated, stored in database with the username and the same series identifier, and a new login
          // cookie containing all three is issued to the user.
          val newToken = RememberMeToken(userId, series, Random.nextLong())
          RememberMeToken.create(newToken)
          Right(UserAuthenticatedWithTokenEvent(userId, newToken.series, newToken.token))
        } else {
          // If the username and series are present but the token does not match, a theft is assumed. The user receives
          // a strongly worded warning and all of the user's remembered sessions are deleted.
          RememberMeToken.removeTokensForUser(userId)
          Left(InvalidSessionCookieFault())
        }
    }.getOrElse(Left(InvalidCredentialsFault()))
  }

}
