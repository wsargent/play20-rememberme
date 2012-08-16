package security

import play.api.cache.{EhCachePlugin, Cache}
import play.api.mvc.RequestHeader
import play.api.Play.current
import com.tersesystems.authentication.SessionStore

object MySessionStore extends SessionStore[String] {

  def saveSession(sessionId: String, userId: String, req: RequestHeader): String = {
    Cache.set(sessionId, userId.toString)
    sessionId
  }

  def lookup(sessionId: String): Option[String] = {
    Cache.getAs[String](sessionId)
  }

  def deleteSession(sessionId: String): Boolean = {
    // Hacking around the Cache API and Ehcache...
    current.plugin[EhCachePlugin].map {
      ehcache =>
        ehcache.cache.remove(sessionId)
    }.getOrElse(false)
  }
}