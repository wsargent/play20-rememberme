package com.tersesystems

package authentication

/**
 * An authentication service trait.  Implement a service with these methods.
 *
 * @author wsargent
 * @since 8/14/12
 */
trait AuthenticationService[UserID] {

  def authenticate(userId:UserID, password:String, rememberMe:Boolean) : Either[AuthenticationFault, UserAuthenticatedEvent[UserID]]

  def authenticateWithCookie(userId:UserID, series:Long, token:Long) : Either[AuthenticationFault, UserAuthenticatedWithTokenEvent[UserID]]
}
