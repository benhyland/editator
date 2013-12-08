package uk.co.bhyland.editator.model

import User.UserId
import Content.Text
import Content.Shadow
import name.fraser.neil.plaintext.diff_match_patch
import name.fraser.neil.plaintext.Patch

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
  
  def applyDiff(userId: UserId, diff: String) = {
    val dmp = new diff_match_patch()
    val patches = dmp.patch_fromText(diff).asInstanceOf[java.util.LinkedList[Patch]]
    val shadow = shadows.get(userId).getOrElse("")
    val patchedShadow = dmp.patch_apply(patches, shadow)
    val patchedText = dmp.patch_apply(patches, text)
    copy(text = patchedText(0).asInstanceOf[Text], shadows = shadows + (userId -> patchedShadow(0).asInstanceOf[Shadow]))
  }
  
  def getDiff(userId: UserId) = {
    val dmp = new diff_match_patch()
    val shadow = shadows.get(userId).getOrElse("")
    dmp.patch_toText(
      dmp.patch_make(shadow, text))
  }
  
  def updateShadow(userId: UserId) = {
    copy(shadows = shadows + (userId -> text.asInstanceOf[Shadow]))
  }
}

object EditatorInstance {
  type RoomId = String
  def apply(key: String) : EditatorInstance = EditatorInstance(key, Set(), "", Map())
  
  // see Neil Fraser on differential sync
  // http://code.google.com/p/google-diff-match-patch/
  // http://neil.fraser.name/writing/sync/ 
}