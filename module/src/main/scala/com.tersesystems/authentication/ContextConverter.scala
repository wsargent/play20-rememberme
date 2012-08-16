package com.tersesystems.authentication

import play.api.mvc.{Request, WrappedRequest}

/**
 *
 * @author wsargent
 * @since 8/15/12
 */

trait ContextConverter[UserInfo] {

  /**
   * Takes a request, and returns a context wrapping the request.
   *
   * @param request
   * @return
   */
  def apply[A](request:Request[A], user:Option[UserInfo]) : Context[A, UserInfo]

}
