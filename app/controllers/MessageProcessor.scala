package controllers

import uk.co.bhyland.editator.messages.EditatorInput
import uk.co.bhyland.editator.messages.EditatorOutput
import uk.co.bhyland.editator.messages.ListRooms
import uk.co.bhyland.editator.messages.UpdateNick
import uk.co.bhyland.editator.messages.ToggleJoinRoom
import java.util.UUID
import uk.co.bhyland.editator.model.EditatorInstance
import uk.co.bhyland.editator.messages.RoomMembershipUpdate

object MessageProcessor {

  def handleInputMessage(state: EditatorState, input: EditatorInput) : (EditatorState, List[EditatorOutput]) = {
    
    val key = input.key.getOrElse(UUID.randomUUID().toString())
    val instance = state.instances.get(key)
    
    def updatedState(instance: Option[EditatorInstance]) = {
      instance.map(newInstance => EditatorState(state.instances.+((key, newInstance)))).getOrElse(state)
    }
    
    val nextState: EditatorState = input match {
      case ListRooms(callback) => {
        callback(state.instances.keys.toList)
        updatedState(None)
      } 
      case UpdateNick(_, user) => {
        updatedState(instance.map(_.changeNick(user)))
        
      }
      case ToggleJoinRoom(_, user, callback) => {
        val newInstance = instance.getOrElse(EditatorInstance(key)).toggleJoin(user)
        callback(newInstance)
        updatedState(Some(newInstance))
      }
    }

    val nextInstance = nextState.instances.get(key)
    val instanceMessages = nextInstance.map(instance =>
       List(RoomMembershipUpdate(instance.members.map(_.name)))
      ).getOrElse(List())
      
    (nextState, instanceMessages)
  }
}