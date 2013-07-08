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

object MessageProcessor {

  def handleInputMessage(state: EditatorState, input: EditatorInput) : (EditatorState, List[EditatorOutput]) = {
    
    def instance(key: String) = state.instances.get(key)
    
    def updatedState(instance: Option[EditatorInstance]) = {
      instance.map(newInstance => state.withInstance(newInstance)).getOrElse(state)
    }
    
    def messages(instance: EditatorInstance) =
      RoomMembershipUpdate(instance.members.map(_.name)) ::
      Nil
    
    val (nextState, newMessages) = input match {
      case AttachUser(key, userId, callback) => {
        val s = state.withUserOutput(userId)
        callback(s.perUserOutput(userId)._2)
        (s, messages(s.instances(key)))
      }
      case ListRooms(callback) => {
        callback(state.instances.keys.toList)
        val s = updatedState(None)
        (s, Nil)
      } 
      case UpdateNick(key, user) => {
        val newInstance = instance(key).map(_.changeNick(user))
        val s = updatedState(newInstance)
        val ms = newInstance.map(messages(_)).getOrElse(Nil)
        (s, ms)
        
      }
      case ToggleJoinRoom(key, user, callback) => {
        val roomKey = if(key.isEmpty) UUID.randomUUID().toString() else key
        val newInstance = instance(roomKey).getOrElse(EditatorInstance(roomKey)).toggleJoin(user)
        callback(newInstance)
        val s = updatedState(Some(newInstance))
        (s, messages(s.instances(key)))
      }
    }
    
    (nextState, newMessages)
  }
}