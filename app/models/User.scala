package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import com.tersesystems.authentication.UserInfoService
import security.MyPasswordService

case class User(email: String, name: String, password: EncryptedPassword)

/**
 * Taken straight from the example Zentasks project.
 */
object User extends UserInfoService[String, User] {

  val passwordService = MyPasswordService

  def register(email: String, name:String, password: String) : User = {
    val encryptedPassword = passwordService.encryptPassword(password)
    val user = User(email, name, encryptedPassword)
    create(user)
  }

  // -- Parsers

  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[String]("user.email") ~
      get[String]("user.name") ~
      get[String]("user.password") map {
      case email ~ name ~ password => {
        val encryptedPassword = EncryptedPassword.parse(password)
        User(email, name, encryptedPassword)
      }
    }
  }

  // -- Queries

  /**
   * Retrieve a User from email.
   */
  def findByEmail(email: String): Option[User] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from user where email = {email}").on(
          'email -> email
        ).as(User.simple.singleOpt)
    }
  }

  /**
   * Retrieve all users.
   */
  def findAll: Seq[User] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from user").as(User.simple *)
    }
  }

  /**
   * Create a User.
   */
  def create(user: User): User = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          insert into user values (
            {email}, {name}, {password}
          )
          """
        ).on(
          'email -> user.email,
          'name -> user.name,
          'password -> user.password.underlying
        ).executeUpdate()

        user
    }
  }

  def lookup(userId: String) = findByEmail(userId)
}
