package controllers

import views._
import play.api.mvc._
import authentication.AuthImpl
import play.api.Logger
import models.User

object Application extends Controller with AuthImpl {

  def logger = Logger(this.getClass)

  def userInfoService = User

  def index = Action {
    implicit request =>
      Ok(html.index("Index Page Rendered"))
  }

  def suspicious = Action {
    implicit request =>
      Ok(html.suspicious())
  }

}
