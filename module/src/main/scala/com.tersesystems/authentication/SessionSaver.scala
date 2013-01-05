package com.tersesystems


package authentication

import play.api.mvc.RequestHeader
import util.Random

/**
 *
 * @author wsargent
 * @since 8/14/12
 */

trait SessionSaver[UserID] {

  def sessionStore: SessionStore[UserID]

  // Save off the sessionId to user id mapping into cache or other fast storage.
  def saveAuthentication(userId: UserID)(implicit req: RequestHeader): String = {
    sessionStore.saveSession(sessionId, userId, req)
  }

  private def sessionId: String = scala.util.Random.shuffle((1 to 10) ++ ('a' to 'z') ++ ('A' to 'Z')).take(20).mkString
}
