package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def editatorBegin = Action {
    Ok(views.html.begin())
  }
}
