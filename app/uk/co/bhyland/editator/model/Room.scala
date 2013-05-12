package uk.co.bhyland.editator.model

import java.util.UUID

case class User(id: String, name: String) {
  def nick(name: String) = copy(name = name)
}

object User {
  def apply(name: Option[String]): User = {
    val id = UUID.randomUUID().toString()
    User(id, name.filterNot(_.isEmpty).getOrElse(id))
  }
}

trait Room {
  def join(user: User): Room
  def leave(id: String): Room
  def isMember(id: String): Boolean
  def toggleJoin(user: User) = if(isMember(user.id)) leave(user.id) else join(user)
  def changeNick(user: User) = if(isMember(user.id)) leave(user.id).join(user) else this
  def members: List[User]
}

case class EditatorRoom(users: Set[User]) extends Room {
  override def join(user: User) = copy(users = (users + user))
  override def leave(id: String) = copy(users = (users.filterNot(_.id == id)))
  override def isMember(id: String) = users.exists(_.id == id)
  override def members = users.toList.sortBy(_.name)
}

object EditatorRoom {
  def apply(): EditatorRoom = EditatorRoom(Set())
}