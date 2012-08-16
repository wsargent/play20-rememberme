package com.tersesystems


package authentication

import play.api.mvc.{ Cookie, Session, RequestHeader }

/**
 * Ease of use session cookie.
 */
object SessionCookie {

  def apply(name: String, value: String)(implicit req: RequestHeader): Cookie = {
    val data = req.session + (name -> value)
    val encoded = Session.encode(Session.serialize(data))
    Cookie(Session.COOKIE_NAME, encoded)
  }
}