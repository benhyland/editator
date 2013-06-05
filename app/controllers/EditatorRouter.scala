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

case class EditatorState(instances: Map[String,EditatorInstance])

object EditatorState {
  def apply(): EditatorState = EditatorState(Map())
}

class EditatorRouter {

  private val (inputEnumerator, inChannel) = Concurrent.broadcast[EditatorInput]

  private val (outputEnumerator, outChannel) = Concurrent.broadcast[JsValue]

  val broadcast: Enumerator[JsValue] = outputEnumerator

  private val processor = Iteratee.fold[EditatorInput, EditatorState](EditatorState()) { (state, input) =>
    
    val (nextState, outputMessages) = MessageProcessor.handleInputMessage(state, input)
    
    outputMessages.foreach { message =>
      outChannel.push(message.asJson)
    }
    
    nextState
  }

  inputEnumerator |>> processor

  def changeNick(key: String, user: User) = inChannel.push(UpdateNick(key, user))

  def toggleJoin(key: Option[String], user: User) = futureResult { p =>
    ToggleJoinRoom(key, user, { room => p.success(Ok(ToggleJoinResponse(room.key, room.isMember(user.id), user).asJson)) })
  }
  
  def currentRooms = futureResult { p =>
    ListRooms({ rooms => p.success(Ok(RoomListUpdate(rooms).asJson)) })
  }
  
  def futureResult(input: Promise[Result] => EditatorInput) = {
    val p = Promise.apply[Result]()
    inChannel.push(input(p))
    p.future
  }
}