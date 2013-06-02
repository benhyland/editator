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

case class EditatorState(users: Set[User], value: String) extends EditatorRoom[EditatorState] with EditatorText[EditatorState] {
  type SELF = EditatorState
  override def self = this
  override def textcopy(value: String) = copy(value = value)
  override def roomcopy(users: Set[User]) = copy(users = users)
}

object EditatorState {
  def apply(): EditatorState = EditatorState(Set(), "")
}

class Processor {

  private val (inputEnumerator, inChannel) = Concurrent.broadcast[EditatorInput]

  private val (outputEnumerator, outChannel) = Concurrent.broadcast[JsValue]

  val broadcast: Enumerator[JsValue] = outputEnumerator

  private val processor = Iteratee.fold[EditatorInput, EditatorState](EditatorState()) { (state, evt) =>
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
    val evt = ToggleJoinRoom(user, { room => p.success(Ok(ToggleJoinResponse(room.isMember(user.id), user).asJson)) })
    inChannel.push(evt)
    p.future
  }
}