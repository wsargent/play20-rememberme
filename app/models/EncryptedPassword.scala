package models

import java.lang.String

/**
 * A type specific password class.
 *
 * Uses type safety for the password without any runtime allocation overhead.
 */
class EncryptedPassword(val underlying:String) extends AnyVal

object EncryptedPassword {
  private val BCRYPT_PATTERN = "\\A\\$2a?\\$\\d\\d\\$[./0-9A-Za-z]{53}".r

  def isValid(hash:String) : Boolean = {
    hash match {
      case BCRYPT_PATTERN() => true
      case _ => false
    }
  }

  def parse(input:String) : EncryptedPassword = {
    if (isValid(input)) {
      new EncryptedPassword(input)
    } else {
      throw new IllegalArgumentException("Encoded password does not look like BCrypt")
    }
  }

}

