package authentication

import controllers.routes

import java.util.UUID
import play.api.data.Forms._
import play.api.data._
import play.api.mvc.Results._
import play.api.mvc._

import play.api.Logger

trait AuthImpl extends SessionSaver {

  def logger: Logger

  def authenticationService: AuthenticationService


}
