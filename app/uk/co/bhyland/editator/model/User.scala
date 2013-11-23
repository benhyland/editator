package uk.co.bhyland.editator.model

import java.util.UUID

case class User(id: User.UserId, name: String) {
  def nick(name: String) = copy(name = name)
}

object User {  
  type UserId = String
  def apply(idOpt: Option[UserId], nameOpt: Option[String]): User = {
    val id = idOpt.filterNot(_.isEmpty).getOrElse(UUID.randomUUID().toString())
    val name = nameOpt.filterNot(_.isEmpty).getOrElse(id)
    User(id, name)
  }
}