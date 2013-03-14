package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation._

import scala.Predef._
import validation.ValidationError

import play.api.data.validation.{Valid, ValidationResult}

/**
 *
 * @author wsargent
 * @since 3/13/13
 */
trait BaseConstraints {

  /**
   * Hooks into the password utility class to validate the various rules you may have for a strong password.
   */
  val password: Mapping[String] = nonEmptyText(minLength = 8).verifying(Constraint("passwordCheck")(passwordCheckFunction))

  def passwordCheckFunction(plainText:String) : ValidationResult = {
    val allNumbers = """\d*""".r
    plainText match {
      case allNumbers() => {
        val error = "Password is all numbers"
        val errors = Seq(ValidationError(error))
        Invalid(errors)
      }
      case _ => Valid
    }
  }

/**
 * Implements a strong password checker by calling out to "pwqcheck", part of the
 * <a href="http://www.openwall.com/passwdqc/">passwdqc</a> package.
 *
 * pwqcheck is a unix command line utility, so you must install passwdqc first and
 * make sure that it is available.
 */

  def externalPasswordCheck(plainText: String) : ValidationResult = {
    import java.io.{OutputStreamWriter, PrintWriter}
    import scala.sys.process._

    // Run through a password enforcement policy based off
    // http://www.openwall.com/articles/PHP-Users-Passwords#enforcing-password-policy
    val pwqcheck = Process("pwqcheck -2")

    var status : ValidationResult = Invalid("Unset")
    def readFromStdout(input: java.io.InputStream) {
      try {
        // XXX Validate the output coming from pwqcheck as a strong password...
        scala.io.Source.fromInputStream(input).getLines().foreach(line => {
          if (line == "OK") {
            status = Valid
          } else {
            status = Invalid("Invalid error")
          }
          Console.println(line)
        })
      } finally {
        input.close()
      }
    }

    def writeToStdin(output : java.io.OutputStream) {
      val writer = new PrintWriter(new OutputStreamWriter(output))
      try {
        writer.write(plainText)
        writer.write("")
      } finally {
        writer.close()
      }
    }

    def readFromStderr(stderr:java.io.InputStream) {
      try {
        scala.io.Source.fromInputStream(stderr).getLines().foreach(println)
      } finally {
        stderr.close()
      }
    }

    val exitValue = pwqcheck.run(new ProcessIO(writeToStdin, readFromStdout, readFromStderr)).exitValue()

    Valid
  }

}
