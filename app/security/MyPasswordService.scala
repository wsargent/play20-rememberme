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

  private val BCRYPT_PATTERN = "\\A\\$2a?\\$\\d\\d\\$[./0-9A-Za-z]{53}".r

  def encryptPassword(plaintext: String) : Password = {
    // Really shouldn't be necessary...
    if (plaintext == null || plaintext.isEmpty) {
      throw new IllegalArgumentException("plaintext cannot be null or empty")
    }

    // You may want to fiddle with the strength and secure random of the salt.
    val salt = BCrypt.gensalt()
    val hashed = BCrypt.hashpw(plaintext, salt)
    new Password(hashed)
  }

  /**
   * Checks the plaintext against the hashed value in the database.
   *
   * @param plaintext
   * @param hashed
   * @return
   */
  def passwordsMatch(plaintext: String, hashed: Password) : Boolean = {
    val underlying = hashed.underlying
    if (underlying == null || underlying.isEmpty) {
      throw new IllegalArgumentException("Encoded password cannot be null or empty")
    }

    underlying match {
      case BCRYPT_PATTERN() => BCrypt.checkpw(plaintext, hashed.underlying)
      case _ => {
        throw new IllegalArgumentException("Encoded password does not look like BCrypt")
      }
    }
  }
}
