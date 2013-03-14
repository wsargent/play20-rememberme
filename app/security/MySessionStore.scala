package security

import play.api.cache.{EhCachePlugin, Cache}
import play.api.mvc.RequestHeader
import play.api.Play.current
import com.tersesystems.authentication.SessionStore

/**
 * A very simple session store that demonstrates how to get a user ID from a session id.
 *
 * Note that this WILL NOT SCALE PAST A SINGLE SERVER.
 */
object MySessionStore extends SessionStore[String] {

  def saveSession(sessionId: String, userId: String, req: RequestHeader): String = {
    Cache.set(sessionId, userId.toString)
    sessionId
  }

  def lookup(sessionId: String): Option[String] = {
    Cache.getAs[String](sessionId)
  }

  def deleteSession(sessionId: String): Boolean = {
    // Hacking around the Cache API and Ehcache.  Note that this will only work for a
    // SINGLE instance of the server, and you should use Redis or similar to scale it up.
    current.plugin[EhCachePlugin].map {
      ehcache =>
        ehcache.cache.remove(sessionId)
    }.getOrElse(false)
  }
}
