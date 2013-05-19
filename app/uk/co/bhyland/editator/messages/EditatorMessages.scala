package uk.co.bhyland.editator.messages

import uk.co.bhyland.editator.model.User
import uk.co.bhyland.editator.model.Room

/** marks input for editator handled by the iteratee state machine */
sealed trait EditatorInput

case class UpdateNick(user: User) extends EditatorInput
case class ToggleJoinRoom(user: User, callback: Room[_] => Unit) extends EditatorInput
case class Talk(user: User, message: String) extends EditatorInput
  
case class Sync(patch: String, checksum: String) extends EditatorInput
case class FullSyncRequest() extends EditatorInput