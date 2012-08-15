package authentication

/**
 * Looks up user information service from the primary key.
 *
 * @author wsargent
 */
trait UserInfoService {

  def lookup(userId:UserID) : Option[UserInfo]

}
