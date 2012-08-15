package models

import play.api.cache.{EhCachePlugin, Cache}
import play.api.mvc.RequestHeader
import play.api.Play.current
import authentication.SessionStore

object SessionCache extends SessionStore {

  def saveSession(sessionId: String, userId: authentication.UserID, req: RequestHeader) : String = {
    Cache.set(sessionId, userId.toString)
    sessionId
  }

  def lookup(sessionId: String) : Option[authentication.UserID] = {
    Cache.getAs[String](sessionId)
  }

  def deleteSession(sessionId: String) : Boolean = {
    // Hacking around the Cache API and Ehcache...
    current.plugin[EhCachePlugin].map {
      ehcache =>
        ehcache.cache.remove(sessionId)
    }.getOrElse(false)
  }
}