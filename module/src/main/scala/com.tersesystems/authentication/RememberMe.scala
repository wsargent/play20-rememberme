package com.tersesystems

package authentication

import play.api.Play
import play.api.mvc.CookieBaker

/**
 * A case class that contains easy mappings to the series, userId and token for the data map.
 *
 * @param data the cookie's map of key / value pairs.
 */
case class RememberMe(data: Map[String, String] = Map.empty[String, String]) {

  object AsLong {
    def apply(stringOption: Option[String]): Option[Long] = {
      stringOption.flatMap { s => apply(s) }
    }

    def apply(s: String): Option[Long] = {
      try {
        Some(s.toLong)
      } catch {
        case e: IllegalArgumentException => None
      }
    }
  }

  import RememberMe._

  def get(key: String) = data.get(key)

  def isEmpty: Boolean = data.isEmpty

  def +(kv: (String, String)) = copy(data + kv)

  def -(key: String) = copy(data - key)

  def series: Option[Long] = AsLong(data.get(SERIES_NAME))

  /**
   * XXX If there's a way to do type conversion given a raw string, I don't know it.  Default to String.
   */
  def userId: Option[String] = data.get(USER_ID_NAME)

  def token: Option[Long] = AsLong(data.get(TOKEN_NAME))

  def apply(key: String) = data(key)
}

/**
 * A cookie baker for a persistent "remember me" cookie.
 */
object RememberMe extends CookieBaker[RememberMe] {

  def apply[UserID](userId: UserID, series: Long, token: Long): RememberMe = {
    val map = Map(
      RememberMe.USER_ID_NAME -> userId.toString,
      RememberMe.SERIES_NAME -> series.toString,
      RememberMe.TOKEN_NAME -> token.toString)
    RememberMe(map)
  }

  val COOKIE_NAME = "REMEMBER_ME"

  val SERIES_NAME = "series"
  val USER_ID_NAME = "userId"
  val TOKEN_NAME = "token"

  val DEFAULT_MAX_AGE = 60*60*24*365 // Set the cookie max age to 1 year

  val emptyCookie = new RememberMe

  override val isSigned = true
  override val secure = Play.maybeApplication.flatMap(_.configuration.getBoolean("rememberMe.secure")).getOrElse(false)
  override val maxAge = Play.maybeApplication.flatMap(_.configuration.getInt("rememberMe.maxAge")).getOrElse(DEFAULT_MAX_AGE)

  def deserialize(data: Map[String, String]) = new RememberMe(data)

  def serialize(rememberme: RememberMe) = rememberme.data
}