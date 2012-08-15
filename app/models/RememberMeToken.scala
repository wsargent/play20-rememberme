package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class RememberMeToken(userId: String, series: Long, token: Long)

object RememberMeToken {

  def create(token: RememberMeToken): RememberMeToken = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
              insert into token values (
                {user_id}, {name}, {password}
              )
          """
        ).on(
          'user_id -> token.userId,
          'series -> token.series,
          'token -> token.token
        ).executeUpdate()

        token
    }
  }

  def remove(token: RememberMeToken) {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
            delete from token where user_id = {userid} and series = {series} and token = {token}
          """
        ).on(
          'user_id -> token.userId,
          'series -> token.series,
          'token -> token.token
        ).executeUpdate()
    }
  }


  def removeTokensForUser(d: authentication.UserID) {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
            delete from token where user_id = {userid}
          """
        ).on(
          'user_id -> d.toString
        ).executeUpdate()
    }
  }

  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[String]("token.userId") ~
      get[Long]("token.series") ~
      get[Long]("token.token") map {
      case userId ~ series ~ token => RememberMeToken(userId, series, token)
    }
  }

  def findByUserIdAndSeries(userId: String, series: Long): Option[RememberMeToken] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from token where user_id = {userId}").on(
          'userId -> userId,
          'series -> series
        ).as(RememberMeToken.simple.singleOpt)
    }
  }


}
