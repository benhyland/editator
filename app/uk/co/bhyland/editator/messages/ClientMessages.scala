package uk.co.bhyland.editator.messages

import org.joda.time.DateTime
import uk.co.bhyland.editator.model.User
import argonaut._
import Argonaut._
import JsonCodec._

trait AsJson {
  def json: Json
}

/** marks output for the client which goes asynchronously via websocket */
sealed trait EditatorOutput extends AsJson

case class RoomMembershipUpdate(nicks: List[String]) extends EditatorOutput {
  override def json = encodeWithMessageType("memberUpdate", this)
}

case class RoomMessageEvent(from: String, timestamp: DateTime, message: String) extends EditatorOutput {
  override def json = encodeWithMessageType("roomMessage", this)      
}

case class SyncEvent(patch: String, checksum: String) extends EditatorOutput {
  override def json = encodeWithMessageType("sync", this)
}

// response messages still go to the client as json
// but are synchronous responses rather than websocket events

case class RoomListUpdate(rooms: List[String]) extends AsJson {
  override def json = this.asJson
}

case class ToggleJoinResponse(roomKey: String, isJoined: Boolean, user: User) extends AsJson {
  override def json = this.asJson
}

// ??
case class FullSyncResponse(content: String)