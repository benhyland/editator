package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee.Iteratee
import play.api.libs.iteratee.Enumerator
import play.api.libs.concurrent.Promise
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  def editatorBegin = Action { request =>
    Ok(views.html.begin(request))
  }

  def editatorJoin = Action(parse.json) { request =>
    val user = request.body \ "nick"
    Ok(user)
  }

  def editatorEvents = WebSocket.using[String] { request =>
    val in = Iteratee.ignore[String]
    val out = Enumerator.repeatM(Promise.timeout("""{ "users": [{"name": "billybob"}, {"name": "whoami"}] }""", 5, TimeUnit.SECONDS))
    (in, out)
  }
}
