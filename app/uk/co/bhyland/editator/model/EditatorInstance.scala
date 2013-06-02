package uk.co.bhyland.editator.model

case class EditatorInstance(users: Set[User], value: String) extends EditatorRoom[EditatorInstance] with EditatorText[EditatorInstance] {
  override def self = this
  override def textcopy(value: String) = copy(value = value)
  override def roomcopy(users: Set[User]) = copy(users = users)
}

object EditatorInstance {
  def apply() : EditatorInstance = EditatorInstance(Set(), "")
}