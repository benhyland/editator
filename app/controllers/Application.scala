package controllers

import play.api.mvc._
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.JsValue

import scala.concurrent.Future

import uk.co.bhyland.editator.model.User

object Application extends Controller {
  
  def logThread(msg: String) = println(Thread.currentThread().getName() + " : " + msg)
  
  val room = new Processor
  
  def editatorBegin = Action { request =>
    Ok(views.html.begin(request))
  }

  def userFromJson(user: JsValue) = {
    val nick = (user \ "nick").asOpt[String]
    val id = (user \ "id").asOpt[String]
    id.map(id => User(id, nick.getOrElse(id)))
      .getOrElse(User(nick))
  }
    
  def editatorToggleJoin = Action(parse.json) { request =>
    val user = userFromJson(request.body)
    val output: Future[Result] = room.toggleJoin(user)
    Async {
      output
    }
  }

  def editatorChangeNick = Action(parse.json) { request =>
    val user = userFromJson(request.body)
    room.changeNick(user)
    Ok("")
  }
  
  def editatorEvents = WebSocket.using[JsValue] { request =>
    val in = Iteratee.ignore[JsValue]
    val out = room.broadcast
    (in, out)
  }
}