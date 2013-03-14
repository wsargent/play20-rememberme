package com.tersesystems.authentication

/**
 *
 * @author wsargent
 * @since 3/13/13
 */
trait PasswordService[P] {

  def encryptPassword(plaintext: String): P

  def passwordsMatch(plaintext: String, hashed: P): Boolean
}
