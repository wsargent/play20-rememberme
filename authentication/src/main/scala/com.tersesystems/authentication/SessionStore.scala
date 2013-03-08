package com.tersesystems

package authentication

import play.api.mvc.RequestHeader

/**
 *
 * @author wsargent
 * @since 8/14/12
 */

trait SessionStore[UserID] {

  def deleteSession(sessionId: String) : Boolean

  def saveSession(sessionId: String, userId: UserID, header: RequestHeader): String

  def lookup(sessionId: String) : Option[UserID]

}
