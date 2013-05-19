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

trait Room[SELF <: Room[SELF]] {
  def self: SELF
  
  def join(user: User): SELF
  def leave(id: String): SELF
  def isMember(id: String): Boolean
  def toggleJoin(user: User) = if(isMember(user.id)) leave(user.id) else join(user)
  def changeNick(user: User) = if(isMember(user.id)) leave(user.id).join(user) else self
  def members: List[User]
}

trait EditatorRoom[SELF <: Room[SELF]] extends Room[SELF] {
  val users: Set[User]
  
  override def join(user: User) = roomcopy(users = (users + user))
  override def leave(id: String) = roomcopy(users = (users.filterNot(_.id == id)))
  override def isMember(id: String) = users.exists(_.id == id)
  override def members = users.toList.sortBy(_.name)
  def roomcopy(users:Set[User]): SELF
}