package controllers

import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.mvc.Results._
import scala.concurrent.Promise
import uk.co.bhyland.editator.messages.EditatorInput
import uk.co.bhyland.editator.messages.UpdateNick
import uk.co.bhyland.editator.messages.ToggleJoinRoom
import uk.co.bhyland.editator.messages.ToggleJoinResponse
import uk.co.bhyland.editator.messages.RoomMembershipUpdate
import uk.co.bhyland.editator.model.User
import uk.co.bhyland.editator.model.EditatorText
import uk.co.bhyland.editator.model.EditatorRoom
import uk.co.bhyland.editator.model.EditatorInstance
import uk.co.bhyland.editator.messages.ListRooms
import uk.co.bhyland.editator.messages.RoomListUpdate

case class EditatorState(instance: EditatorInstance)

object EditatorState {
  def apply(): EditatorState = EditatorState(EditatorInstance())
}

class Processor {

  private val (inputEnumerator, inChannel) = Concurrent.broadcast[EditatorInput]

  private val (outputEnumerator, outChannel) = Concurrent.broadcast[JsValue]

  val broadcast: Enumerator[JsValue] = outputEnumerator

  private val processor = Iteratee.fold[EditatorInput, EditatorState](EditatorState()) { (state, evt) =>
    val nextInstance = evt match {
      case ListRooms(callback) => {
        callback(List(state.instance.toString))
        state.instance
      } 
      case UpdateNick(user) => state.instance.changeNick(user)
      case ToggleJoinRoom(user, callback) => {
        val s = state.instance.toggleJoin(user)
        callback(s)
        s
      }
    }
    outChannel.push(RoomMembershipUpdate(nextInstance.members.map(_.name)).asJson)
    EditatorState(nextInstance)
  }

  inputEnumerator |>> processor

  def changeNick(user: User) = inChannel.push(UpdateNick(user))

  def toggleJoin(user: User) = {
    val p = Promise.apply[Result]()
    val evt = ToggleJoinRoom(user, { room => p.success(Ok(ToggleJoinResponse(room.isMember(user.id), user).asJson)) })
    inChannel.push(evt)
    p.future
  }
  
  def currentRooms = {
    val p = Promise.apply[Result]()
    val evt = ListRooms({ rooms => p.success(Ok(RoomListUpdate(rooms).asJson)) })
    inChannel.push(evt)
    p.future
  }
}