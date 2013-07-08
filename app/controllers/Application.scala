package controllers

import play.api.mvc._
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.JsValue
import scala.concurrent.Future
import uk.co.bhyland.editator.model.User
import uk.co.bhyland.editator.messages.JsonCodec._
import argonaut._
import Argonaut._

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
      
  def editatorToggleJoin = Action(parse.json) { request =>
    val parsed = decodeWithRoomKey[User](request.body.toString)
    val output: Future[Result] =
      parsed.map { case (key, user) => router.toggleJoin(key, user) }
    	.getOrElse(Future.successful(BadRequest("")))
    Async {
      output
    }
  }

  def editatorChangeNick = Action(parse.json) { request =>
    val parsed = decodeWithRoomKey[User](request.body.toString)
    parsed.foreach{ case (key, user) => router.changeNick(key, user) }
    Ok("")
  }
  
  def editatorEvents(roomKey: String, userId: String) = WebSocket.using[JsValue] { request =>
    val in = Iteratee.ignore[JsValue]
    val out = router.broadcastFor(roomKey, userId)
    (in, out)
  }  
}