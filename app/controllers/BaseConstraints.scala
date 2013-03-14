package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation._

import scala.Predef._
import validation.ValidationError

import play.api.data.validation.Valid
import play.api.{Play, Logger}

/**
 * Adds some decent password checking constraints.
 *
 * There are two password checkers.
 *
 * The weak one, 'weakPassword' uses regular expressions and is mostly useless.
 *
 * The strong one, 'strongPassword' calls out to "pwqcheck", part of the Openwall
 * <a href="http://www.openwall.com/passwdqc/">passwdqc</a> package.  pwqcheck
 * is a unix command line utility, so you must install passwdqc first and
 * make sure that it is available.  This is easier on Linux based systems,
 * but you can use a Mac variant by installing
 * <a href="https://github.com/iphoting/passwdqc-mac">passwdqc-mac</a>.  You can
 * change the pwqcheck parameters by setting "authentication.pwqcheck" in application.conf.
 *
 * Note that this has NOT been tried to scale in a production environment.
 *
 * @author wsargent
 */
trait BaseConstraints
{
  def logger: Logger

  val internalPasswordConstraint: Constraint[String] = Constraint("constraints.internalPassword")({
    plaintext =>
      val errors = internalPasswordChecks(plaintext)
      if (errors.isEmpty) {
        Valid
      } else {
        Invalid(errors)
      }
  }
  )

  val externalPasswordConstraint: Constraint[String] = Constraint("constraints.externalPassword")({
    plaintext =>
      val errors = externalPasswordCheck(plaintext)
      if (errors.isEmpty) {
        Valid
      } else {
        Invalid(errors)
      }
  }
  )

  /**
   * Uses a weak password checker for validation.
   */
  val weakPassword: Mapping[String] = nonEmptyText(minLength = models.Password.MIN_PASSWORD_LENGTH).verifying(internalPasswordConstraint)

  /**
   * Uses a strong password checker for validation.
   */
  val strongPassword: Mapping[String] = nonEmptyText(minLength = models.Password.MIN_PASSWORD_LENGTH).verifying(externalPasswordConstraint)

  /**
   * An example of an internal password check, for when you don't want a dependency on passwdqc.
   *
   * @param plainText the plaintext password.
   * @return the validation result.
   */
  def internalPasswordChecks(plainText: String): Seq[ValidationError] = {
    // XXX want to abstract this out to run through a chain, but this shows the idea.
    val allNumbers = """\d*""".r
    val allWords = """[A-Za-z]*""".r
    plainText match {
      case allNumbers() => Seq(ValidationError("Password is all numbers"))
      case allWords() => Seq(ValidationError("Password is all letters"))
      case _ => Seq()
    }
  }

  /**
   * Calls out to an external process "pwqcheck" (assumed to be on the path) and reads from any errors.
   * Taken from <a href="http://www.openwall.com/articles/PHP-Users-Passwords#enforcing-password-policy">enforcing password policy</a>.
   *
   * @param plainText The plaintext password to check.
   * @return validation errors returned from the stdout of pwqcheck.
   */
  def externalPasswordCheck(plainText: String): Seq[ValidationError] = {
    import java.io.{OutputStreamWriter, PrintWriter}
    import scala.sys.process._
    import collection.mutable.ArrayBuffer

    // only check one password.
    val pwqcheckExec = Play.maybeApplication.flatMap(_.configuration.getString("authentication.pwqcheck")).getOrElse("pwqcheck -1")
    val pwqcheck = Process(pwqcheckExec)

    // Really don't like using a mutable data structure here.
    var errorList = ArrayBuffer[ValidationError]()
    def readFromStdout(input: java.io.InputStream) {
      try {
        scala.io.Source.fromInputStream(input).getLines().foreach(line => {
          if (line != "OK") {
            errorList += ValidationError(line)
          }
          logger.debug(line)
        })
      } finally {
        input.close()
      }
    }

    def writeToStdin(output: java.io.OutputStream) {
      val writer = new PrintWriter(new OutputStreamWriter(output))
      try {
        writer.println(plainText)
      } finally {
        writer.close()
      }
    }

    def readFromStderr(stderr: java.io.InputStream) {
      try {
        scala.io.Source.fromInputStream(stderr).getLines().foreach(line => {
          logger.error(line)
        })
      } finally {
        stderr.close()
      }
    }

    val exitValue = pwqcheck.run(new ProcessIO(writeToStdin, readFromStdout, readFromStderr)).exitValue()
    logger.debug("exitValue = " + exitValue)

    errorList.toSeq
  }

}
