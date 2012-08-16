package com.tersesystems

package authentication

import play.api.mvc._

/**
 * This trait extends WrappedRequest to associate authenticated user information with the request.
 *
 * You should create your own context that extends Context
 */
trait Context[A, UserInfo] extends Request[A] {

  def request: Request[A]

  def me: Option[UserInfo]

  def isAuth = me.isDefined

  def is(user: UserInfo) = me == Some(user)
}