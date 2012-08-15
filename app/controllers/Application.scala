package controllers

import views._
import play.api.mvc._

object Application extends Controller with BaseActions {

  def index = Open {
    implicit ctx =>
      Ok(html.index())
  }

}
