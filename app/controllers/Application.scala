package controllers

import play.api.mvc._
import play.api.libs.iteratee.Iteratee
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.JsValue

import scala.concurrent.Future
import scala.concurrent.Promise

import uk.co.bhyland.editator.model.User

import uk.co.bhyland.editator.model.EditatorRoom
import uk.co.bhyland.editator.model.EditatorText

import uk.co.bhyland.editator.messages.EditatorInput
import uk.co.bhyland.editator.messages.ToggleJoinRoom
import uk.co.bhyland.editator.messages.UpdateNick

import uk.co.bhyland.editator.messages.RoomMembershipUpdate
import uk.co.bhyland.editator.messages.ToggleJoinResponse

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
    
  case class EditatorState(users: Set[User], value: String) extends EditatorRoom[EditatorState] with EditatorText[EditatorState] {
    type SELF = EditatorState
    override def self = this
    override def textcopy(value: String) = copy(value = value)
    override def roomcopy(users: Set[User]) = copy(users = users)
  }
  object EditatorState {
    def apply() : EditatorState = EditatorState(Set(), "")
  }
  
  class Processor {
        
    private val (inputEnumerator, inChannel) = Concurrent.broadcast[EditatorInput]
    
    private val (outputEnumerator, outChannel) = Concurrent.broadcast[JsValue]
    
    val broadcast: Enumerator[JsValue] = outputEnumerator
    
    private val processor = Iteratee.fold[EditatorInput, EditatorState](EditatorState()){ (state, evt) =>
      val nextState = evt match {
        case UpdateNick(user) => state.changeNick(user)
        case ToggleJoinRoom(user, callback) => {
          val s = state.toggleJoin(user)
          callback(s)
          s
        }
      }
      outChannel.push(RoomMembershipUpdate(nextState.members.map(_.name)).asJson)
      nextState
    }
    
    inputEnumerator |>> processor
        
    def changeNick(user: User) = inChannel.push(UpdateNick(user))
    
    def toggleJoin(user: User) = {
      val p = Promise.apply[Result]()
      val evt = ToggleJoinRoom(user, {room => p.success(Ok(ToggleJoinResponse(room.isMember(user.id), user).asJson))})
      inChannel.push(evt)
      p.future
    } 
  }
}