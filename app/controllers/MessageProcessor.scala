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
import uk.co.bhyland.editator.messages.FullSyncRequest
import uk.co.bhyland.editator.messages.FullSyncEvent
import uk.co.bhyland.editator.messages.SyncEvent
import uk.co.bhyland.editator.messages.DifferentialSyncRequest
import uk.co.bhyland.editator.model.EditatorInstance.RoomId

object MessageProcessor {

  def handleInputMessage(state: EditatorState, input: EditatorInput) : (EditatorState, List[EditatorOutput], () => Unit) = {
    
    def updatedState(instance: Option[EditatorInstance]) = {
      instance.map(newInstance => state.withInstance(newInstance)).getOrElse(state)
    }
    
    def messages(instance: Option[EditatorInstance]): List[EditatorOutput] =
      instance.map(i =>
        RoomMembershipUpdate(i.members) ::
        Nil
      ).getOrElse(Nil) 
    
    def noOp = () => ()
      
    input match {
      case AttachUser(key, userId, callback) => {
        val s = state.withUserOutput(userId)
        callback(s.perUserOutput(userId)._2)
        (s, messages(s.instances.getInstanceForRoom(key)), noOp)
      }
      case UnattachUser(key, userId) => {
        val newInstance = state.instances.getInstanceForRoom(key).map(_.leave(userId))
        val s = updatedState(newInstance).dropUserOutput(userId)
        (s, messages(s.instances.getInstanceForRoom(key)), noOp)
      }
      case ListRooms(callback) => {        
        (state, Nil, { () => callback(state.instances.roomIds.toList) })
      } 
      case UpdateNick(key, user) => {
        val newInstance = state.instances.getInstanceForRoom(key).map(_.changeNick(user))
        val s = updatedState(newInstance)
        val ms = messages(newInstance)
        (s, ms, noOp)
        
      }
      case ToggleJoinRoom(key, user, callback) => {
        val roomKey = if(key.isEmpty) UUID.randomUUID().toString() else key
        val newInstance = state.instances.getInstanceForRoom(roomKey).getOrElse(EditatorInstance(roomKey)).toggleJoin(user)
        val s = updatedState(Some(newInstance))
        (s, messages(s.instances.getInstanceForRoom(key)), { () => callback(newInstance) })
      }
      case Talk(key, user, message) => {
        (state, List(RoomMessageEvent(key, user.id, DateTime.now, message)), noOp)
      }
      case FullSyncRequest(key, userId) => {
        (state, List(FullSyncEvent(userId,
          state.instances.getInstanceForRoom(key)
          .flatMap(_.shadows.get(userId)).getOrElse(""))
          ), noOp)
      }
      case DifferentialSyncRequest(key, userId, diff) => {
        val patchedInstance = state.instances.getInstanceForRoom(key).map(_.applyDiff(userId, diff))
        val reverseDiff = patchedInstance.map(_.getDiff(userId)).getOrElse("")
        val newInstance = patchedInstance.map(_.updateShadow(userId))
        val s = updatedState(newInstance)
        (s, List(SyncEvent(userId, reverseDiff)), noOp)
      }
    }
  }
}