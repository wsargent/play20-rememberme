package security

import org.mindrot.jbcrypt.BCrypt
import com.tersesystems.authentication.PasswordService
import models.Password

/**
 * A password services that uses <a href="http://www.mindrot.org/projects/jBCrypt/">jBCrypt</a>.
 *
 *
 **/
object MyPasswordService extends PasswordService[Password] {

  def encryptPassword(plaintext: String) : Password = {
    val hashed = BCrypt.hashpw(plaintext, BCrypt.gensalt())
    new Password(hashed)
  }

  def passwordsMatch(plaintext: String, hashed: Password) : Boolean = {
    BCrypt.checkpw(plaintext, hashed.underlying)
  }
}
