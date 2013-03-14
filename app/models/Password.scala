package models

import java.lang.String

/**
 * A type specific password class.
 *
 * Uses type safety for the password without any runtime allocation overhead.
 */
class Password(val underlying:String) extends AnyVal

object Password {
  val MIN_PASSWORD_LENGTH = 8

  def isValid(input:String) : Boolean = {
     (input.size >= MIN_PASSWORD_LENGTH)
  }

  def parse(input:String) : Password = {
    if (isValid(input))
      new Password(input)
    else
      throw new RuntimeException("Invalid password")
  }

}

