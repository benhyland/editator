package controllers

import play.api.mvc._
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.JsValue
import scala.concurrent.Future
import uk.co.bhyland.editator.model.User
import uk.co.bhyland.editator.messages.JsonCodec._
import uk.co.bhyland.editator.messages.Talk

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
    val parsed = decodeWithRoomKeyAs[User](request.body.toString)
    val output: Future[Result] =
      parsed.map { case (key, user) => router.toggleJoin(key, user) }
    	.getOrElse(Future.successful(BadRequest("")))
    Async {
      output
    }
  }

  def editatorChangeNick = Action(parse.json) { request =>
    val parsed = decodeWithRoomKeyAs[User](request.body.toString)
    parsed.foreach{ case (key, user) => router.changeNick(key, user) }
    Ok("")
  }

  def editatorChat = Action(parse.json) { request =>
    val parsed = decodeAs[Talk](request.body.toString)
    parsed.foreach{ t => router.talk(t) }
    Ok("")
  }
  
  def editatorEvents(roomKey: String, userId: String) = WebSocket.using[JsValue] { request =>
    val in = Iteratee.foreach[JsValue](json => {
      import argonaut._
      import Argonaut._
      val parsed = json.toString.decodeOption(syncDecode)
      parsed.foreach{ r => router.syncRequest(r) }
    }).mapDone { _ => router.unattach(roomKey, userId) }
    val out = router.broadcastFor(roomKey, userId)
    (in, out)
  }  
}