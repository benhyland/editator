package uk.co.bhyland.editator.model

import EditatorInstance.RoomId

class EditatorInstances(byRoomId: Map[RoomId, EditatorInstance]) {
  def this() = this(Map())
  
  def add(instance: EditatorInstance) = new EditatorInstances(byRoomId + (instance.key -> instance))
  
  def getInstanceForRoom(roomId: RoomId) = byRoomId.get(roomId)
  
  def roomIds = byRoomId.keys
}