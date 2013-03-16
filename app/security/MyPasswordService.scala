package security

import org.mindrot.jbcrypt.BCrypt
import com.tersesystems.authentication.PasswordService
import models.EncryptedPassword

/**
 * A password services that uses <a href="http://www.mindrot.org/projects/jBCrypt/">jBCrypt</a>.
 *
 *
 **/
object MyPasswordService extends PasswordService[EncryptedPassword] {

  def encryptPassword(plaintext: String) : EncryptedPassword = {
    // Really shouldn't be necessary...
    if (plaintext == null || plaintext.isEmpty) {
      throw new IllegalArgumentException("plaintext cannot be null or empty")
    }

    // You may want to fiddle with the strength and secure random of the salt.
    val salt = BCrypt.gensalt()
    val hashed = BCrypt.hashpw(plaintext, salt)
    new EncryptedPassword(hashed)
  }

  /**
   * Checks the plaintext against the hashed value in the database.
   *
   * @param plaintext
   * @param hashed
   * @return
   */
  def passwordsMatch(plaintext: String, hashed: EncryptedPassword) : Boolean = {
    if (hashed == null) {
      throw new IllegalArgumentException("Encoded password cannot be null or empty")
    }

    BCrypt.checkpw(plaintext, hashed.underlying)
  }
}
