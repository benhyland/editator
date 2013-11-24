package uk.co.bhyland.editator.model

import User.UserId
import Content.Text
import Content.Shadow

// TODO: differentialsync state
// http://code.google.com/p/google-diff-match-patch/
// http://neil.fraser.name/writing/sync/

case class EditatorInstance(
    key: EditatorInstance.RoomId,
    users: Set[User],
    text: Text,
    shadows: Map[UserId, Shadow]) {
  
  def isMember(userId: UserId) = users.exists(_.id == userId)
  def members = users.toList.sortBy(_.name)
  def leave(userId: UserId) = copy(users = users.filterNot(_.id == userId), shadows = shadows - userId)
  def join(user: User) = copy(users = (users + user), shadows = shadows + (user.id -> text))
  def toggleJoin(user: User) = if(isMember(user.id)) leave(user.id) else join(user)
  def changeNick(user: User) = if(isMember(user.id)) {
    val shadow = shadows(user.id)
    leave(user.id).join(user).copy(shadows = shadows + (user.id -> shadow))
  } else this
}

object EditatorInstance {
  type RoomId = String
  def apply(key: String) : EditatorInstance = EditatorInstance(key, Set(), "", Map())
}

//  
//  def value: String
//  def diffWith(other: Text[SELF]): Patch
//  def calculateChecksum: Checksum
//  def applyPatch(patch: Patch): SELF
//  
