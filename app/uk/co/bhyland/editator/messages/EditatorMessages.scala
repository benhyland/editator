package uk.co.bhyland.editator.messages

import uk.co.bhyland.editator.model.User
import uk.co.bhyland.editator.model.Room
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsValue

/** marks input for editator handled by the iteratee state machine */
sealed trait EditatorInput

sealed trait HasKey extends EditatorInput {
  def roomKey: String
}

case class AttachUser(roomKey: String, userId: String, callback: Enumerator[JsValue] => Unit) extends HasKey
case class UnattachUser(roomKey: String, userId: String) extends HasKey
case class ListRooms(callback: List[String] => Unit) extends EditatorInput
case class UpdateNick(roomKey: String, user: User) extends HasKey
case class ToggleJoinRoom(roomKey: String, user: User, callback: Room[_] => Unit) extends HasKey
case class Talk(roomKey: String, user: User, message: String) extends HasKey
  
case class Sync(roomKey: String, patch: String, checksum: String) extends HasKey
case class FullSyncRequest(roomKey: String, userId: String) extends HasKey
case class DifferentialSyncRequest(roomKey: String, userId: String) extends HasKey