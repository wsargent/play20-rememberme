package authentication

/**
 *
 * @author wsargent
 * @since 8/14/12
 */

trait AuthenticationService {

  def authenticate(userId:UserID, password:String, rememberMe:Boolean) : Either[AuthenticationFault, UserAuthenticatedEvent]

  def authenticateWithCookie(userId:UserID, series:Long, token:Long) : Either[AuthenticationFault, UserAuthenticatedWithTokenEvent]
}
