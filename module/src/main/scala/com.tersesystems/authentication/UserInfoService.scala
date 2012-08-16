package com.tersesystems

package authentication

/**
 * Looks up user information service from the primary key.
 *
 * @author wsargent
 */
trait UserInfoService[UserID, UserInfo] {

  def lookup(userId:UserID) : Option[UserInfo]

}
