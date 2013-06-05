package uk.co.bhyland.editator.messages

import uk.co.bhyland.editator.model.User
import uk.co.bhyland.editator.model.Room

/** marks input for editator handled by the iteratee state machine */
sealed trait EditatorInput {
  def key: Option[String] = None
}

sealed trait HasKey extends EditatorInput {
  def roomKey: String
  override def key: Option[String] = Some(roomKey)
}

case class ListRooms(callback: List[String] => Unit) extends EditatorInput
case class UpdateNick(roomKey: String, user: User) extends EditatorInput with HasKey
case class ToggleJoinRoom(override val key: Option[String], user: User, callback: Room[_] => Unit) extends EditatorInput
case class Talk(roomKey: String, user: User, message: String) extends EditatorInput with HasKey
  
case class Sync(roomKey: String, patch: String, checksum: String) extends EditatorInput with HasKey
case class FullSyncRequest(roomKey: String) extends EditatorInput with HasKey