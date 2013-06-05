package controllers

import play.api.mvc._
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.JsValue

import scala.concurrent.Future

import uk.co.bhyland.editator.model.User

object Application extends Controller {
  
  val router = new EditatorRouter
  
  def editatorBegin = Action { request =>
    Ok(views.html.begin(request))
  }

  def editatorRooms = Action { request =>
    val output = router.currentRooms
    Async {
      output
    }
  }
  
  def userFromJson(user: JsValue) = {
    val nick = (user \ "nick").asOpt[String]
    val id = (user \ "id").asOpt[String]
    id.map(id => User(id, nick.getOrElse(id)))
      .getOrElse(User(nick))
  }
    
  def editatorToggleJoin = Action(parse.json) { request =>
    val user = userFromJson(request.body)
    val key = (request.body \ "key").asOpt[String]
    val output: Future[Result] = router.toggleJoin(key, user)
    Async {
      output
    }
  }

  def editatorChangeNick = Action(parse.json) { request =>
    val user = userFromJson(request.body)
    val key = (request.body \ "key").asOpt[String]
    key.foreach{ key =>
    	router.changeNick(key, user)
    }
    Ok("")
  }
  
  def editatorEvents = WebSocket.using[JsValue] { request =>
    val in = Iteratee.ignore[JsValue]
    // TODO: get appropriate event stream for instance
    val out = router.broadcast
    (in, out)
  }
}