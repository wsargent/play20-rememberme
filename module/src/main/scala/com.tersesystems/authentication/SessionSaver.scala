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
    sessionStore.saveSession(Random.nextString(20), userId, req)
  }

}
