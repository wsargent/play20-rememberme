package com.tersesystems.authentication

/**
 *
 * @author wsargent
 * @since 8/15/12
 */

trait UserIdConverter[UserID] {

  def apply(userId:String) : UserID

}
