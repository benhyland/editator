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
import uk.co.bhyland.editator.messages.AsJson
import argonaut.Json
import uk.co.bhyland.editator.messages.JsonCodec.AsPlayJsValue
import play.api.libs.iteratee.Concurrent.Channel
import uk.co.bhyland.editator.messages.EditatorOutput
import uk.co.bhyland.editator.messages.AttachUser
import scala.concurrent.duration._
import scala.concurrent.Await
import uk.co.bhyland.editator.messages.UnattachUser

case class EditatorState(
    inputEnumerator: Enumerator[EditatorInput],
    inputChannel: Channel[EditatorInput],
    perUserOutput: Map[String, (Channel[JsValue], Enumerator[JsValue])],
    instances: Map[String,EditatorInstance]) {
  
  def withInstance(instance: EditatorInstance) =
    copy(instances = (instances + ((instance.key, instance))))
  
  def withUserOutput(userId: String) = {
    val outs = Concurrent.broadcast[JsValue]
    copy(perUserOutput = perUserOutput + (userId -> outs.swap))
  }
  def dropUserOutput(userId: String) = {
    perUserOutput.get(userId).map(_._1).foreach(_.eofAndEnd)
    copy(perUserOutput = perUserOutput - userId)
  }
  def channelsFor(mesage: EditatorOutput) = perUserOutput.values.map(_._1)
}

object EditatorState {
  def apply(): EditatorState = {
    val (inputEnumerator, inChannel) = Concurrent.broadcast[EditatorInput]
    EditatorState(inputEnumerator, inChannel, Map(), Map())
  }
}

class EditatorRouter {

  private val (inChannel, processor) = {
    val state = EditatorState()
    val p = Iteratee.fold[EditatorInput, EditatorState](state) { (state, input) =>
	  val (nextState, outputMessages) = MessageProcessor.handleInputMessage(state, input)
	  for {
	    m <- outputMessages
	    json = m.json.forPlay	    
	    c <- nextState.channelsFor(m)
	  } {
	    c.push(json)
	  }
	  nextState
    }
    
    state.inputEnumerator |>> p
    
    (state.inputChannel, p)
  }  

  def changeNick(key: String, user: User) = inChannel.push(UpdateNick(key, user))

  def toggleJoin(key: String, user: User) = future[Result] { p =>
    ToggleJoinRoom(key, user, { room => p.success(Ok(ToggleJoinResponse(room.key, room.isMember(user.id), user).json.forPlay)) })
  }
  
  def currentRooms = future[Result] { p =>
    ListRooms({ rooms => p.success(Ok(RoomListUpdate(rooms).json.forPlay)) })
  }
  
  def broadcastFor(roomKey: String, userId: String) = {
    val f = future[Enumerator[JsValue]] { p =>
      AttachUser(roomKey, userId, { output => p.success(output) })
    }
    Await.result(f, 1 seconds)
  }
  
  def unattach(roomKey: String, userId: String) = inChannel.push(UnattachUser(roomKey, userId))
  
  def future[A](input: Promise[A] => EditatorInput) = {
    val p = Promise.apply[A]()
    inChannel.push(input(p))
    p.future
  }
}