package uk.co.bhyland.editator.model

trait Patch
trait Checksum

// TODO: differentialsync state
// http://code.google.com/p/google-diff-match-patch/
// http://neil.fraser.name/writing/sync/

trait Text[SELF <: Text[SELF]] {
  
  def value: String
  def diffWith(other: Text[SELF]): Patch
  def calculateChecksum: Checksum
  def applyPatch(patch: Patch): SELF
}

trait EditatorText[SELF <: Text[SELF]] extends Text[SELF] {
  val value: String
  override def diffWith(other: Text[SELF]) = ???
  override def calculateChecksum = ???
  override def applyPatch(patch: Patch) = textcopy(value = value + ???)
  def textcopy(value: String): SELF
}
