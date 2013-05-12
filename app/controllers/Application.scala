package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee.Iteratee
import play.api.libs.iteratee.Enumerator
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.JsValue
import play.api.libs.iteratee.Concurrent
import uk.co.bhyland.editator.model.User
import uk.co.bhyland.editator.model.Room
import uk.co.bhyland.editator.model.EditatorRoom
import scala.concurrent.Future
import scala.concurrent.Promise
import play.api.libs.json.Json.toJson

object Application extends Controller {
  
  val room = new RoomProcessor
  
  def editatorBegin = Action { request =>
    Ok(views.html.begin(request))
  }

  def userFromJson(user: JsValue) = {
    val nick = (user \ "nick").asOpt[String]
    val id = (user \ "id").asOpt[String]
    id.map(id => User(id, nick.getOrElse(id)))
      .getOrElse(User(nick))
  }
  
  // this is fugly :(
  class ToggleJoinOutput(isJoined: Boolean, user: User) {
    def json = toJson(Map(
      "isJoined" -> toJson(isJoined),
      "user" -> toJson(Map(
        "id" -> toJson(user.id),
        "nick" -> toJson(user.name)
        ))
    ))
  }
  
  def editatorToggleJoin = Action(parse.json) { request =>
    val user = userFromJson(request.body)
    
    val output: Future[ToggleJoinOutput] = room.toggleJoin(user)
    Async {
      output.map { tjo => Ok(tjo.json) }
    }
  }

  def editatorChangeNick = Action(parse.json) { request =>
    val user = userFromJson(request.body)

    room.changeNick(user)
    Ok("")
  }

  sealed trait UpdateEvent
  case class MemberUpdate(users: List[String]) extends UpdateEvent
  
  def editatorEvents = WebSocket.using[JsValue] { request =>
    val in = Iteratee.ignore[JsValue]
    val out = room.broadcast.map { case evt: MemberUpdate =>
      toJson(Map(
        "type" -> toJson("memberUpdate"),
        "members" -> toJson(evt.users)          
      ))
    }
    (in, out)
  }
  
  sealed trait RoomEvent
  case class Nick(user: User) extends RoomEvent
  
  case class ToggleJoin(user: User, callback: Room => Unit) extends RoomEvent
  
  class RoomProcessor {
    
    // imperative input channel
    private val (inputEnumerator, inChannel) = Concurrent.broadcast[RoomEvent]
    
    private val (outputEnumerator, outChannel) = Concurrent.broadcast[Room]
    
    val broadcast: Enumerator[UpdateEvent] = outputEnumerator.map{ room =>
      MemberUpdate(room.members.map(_.name))
    }
    
    private val processor = Iteratee.fold[RoomEvent, Room](EditatorRoom()){ (room, evt) =>
      println(evt)
      val newRoom = evt match {
        case Nick(user) => room.changeNick(user)
        case ToggleJoin(user, callback) => {
          val r = room.toggleJoin(user)
          callback(r)
          r
        }
      }
      outChannel.push(newRoom)
      newRoom
    }
    
    inputEnumerator |>> processor
    
    // TODO: for each enumerator println, map enumerator,
    // iteratee state machine, figure out how to get response at the same time as passing the event in, hook up broadcast to sockets,
    // figure out how to do most of the processing in unicast.
    
    def changeNick(user: User) = inChannel.push(Nick(user))
    
    def toggleJoin(user: User) = {
      val p = Promise.apply[ToggleJoinOutput]()
      val evt = ToggleJoin(user,
          {room => p.success(new ToggleJoinOutput(room.isMember(user.id), user))})
      inChannel.push(evt)
      p.future
    } 
  }
}
