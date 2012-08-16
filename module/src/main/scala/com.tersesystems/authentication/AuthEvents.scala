package com.tersesystems

package authentication

/**
 *
 * @author wsargent
 * @since 8/14/12
 */

trait AuthenticationEvent

case class UserAuthenticatedEvent[UserID](userId: UserID, series: Option[Long] = None, token: Option[Long] = None) extends AuthenticationEvent

case class UserAuthenticatedWithTokenEvent[UserID](userId : UserID, series:Long, token:Long) extends AuthenticationEvent

trait AuthenticationFault

case class InvalidSessionCookieFault() extends AuthenticationFault

case class InvalidCredentialsFault() extends AuthenticationFault
