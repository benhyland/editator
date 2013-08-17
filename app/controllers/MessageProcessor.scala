package controllers

import uk.co.bhyland.editator.messages.EditatorInput
import uk.co.bhyland.editator.messages.EditatorOutput
import uk.co.bhyland.editator.messages.ListRooms
import uk.co.bhyland.editator.messages.UpdateNick
import uk.co.bhyland.editator.messages.ToggleJoinRoom
import java.util.UUID
import uk.co.bhyland.editator.model.EditatorInstance
import uk.co.bhyland.editator.messages.RoomMembershipUpdate
import uk.co.bhyland.editator.messages.AttachUser
import uk.co.bhyland.editator.messages.UnattachUser
import uk.co.bhyland.editator.messages.Talk
import uk.co.bhyland.editator.messages.RoomMessageEvent
import org.joda.time.DateTime

object MessageProcessor {

  def handleInputMessage(state: EditatorState, input: EditatorInput) : (EditatorState, List[EditatorOutput], () => Unit) = {
    
    def instance(key: String) = state.instances.get(key)
    
    def updatedState(instance: Option[EditatorInstance]) = {
      instance.map(newInstance => state.withInstance(newInstance)).getOrElse(state)
    }
    
    def messages(instance: EditatorInstance) =
      RoomMembershipUpdate(instance.members) ::
      Nil
    
    def noOp = () => ()
      
    input match {
      case AttachUser(key, userId, callback) => {
        val s = state.withUserOutput(userId)
        callback(s.perUserOutput(userId)._2)
        (s, messages(s.instances(key)), noOp)
      }
      case UnattachUser(key, userId) => {
        val s = state.dropUserOutput(userId)
        (s, messages(s.instances(key)), noOp)
      }
      case ListRooms(callback) => {        
        (state, Nil, { () => callback(state.instances.keys.toList) })
      } 
      case UpdateNick(key, user) => {
        val newInstance = instance(key).map(_.changeNick(user))
        val s = updatedState(newInstance)
        val ms = newInstance.map(messages(_)).getOrElse(Nil)
        (s, ms, noOp)
        
      }
      case ToggleJoinRoom(key, user, callback) => {
        val roomKey = if(key.isEmpty) UUID.randomUUID().toString() else key
        val newInstance = instance(roomKey).getOrElse(EditatorInstance(roomKey)).toggleJoin(user)
        val s = updatedState(Some(newInstance))
        (s, messages(s.instances(key)), { () => callback(newInstance) })
      }
      case Talk(key, user, message) => {
        (state, List(RoomMessageEvent(key, user.id, DateTime.now, message)), noOp)
      }
    }
  }
}