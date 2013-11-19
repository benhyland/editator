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

case class RoomMembershipUpdate(users: List[User]) extends EditatorOutput {
  override def json = encodeWithMessageTypeAs("memberUpdate", this)
}

case class RoomMessageEvent(roomId: String, from: String, timestamp: DateTime, message: String) extends EditatorOutput {
  override def json = encodeWithMessageTypeAs("chatMessage", this)      
}

case class SyncEvent(patch: String, checksum: String) extends EditatorOutput {
  override def json = encodeWithMessageTypeAs("sync", this)
}

case class FullSyncEvent(text: String) extends EditatorOutput {
  override def json = encodeWithMessageTypeAs("resync", this)
}

// response messages still go to the client as json
// but are synchronous responses rather than websocket events

case class RoomListUpdate(rooms: List[String]) extends AsJson {
  override def json = this.asJson
}

case class ToggleJoinResponse(roomKey: String, isJoined: Boolean, user: User) extends AsJson {
  override def json = this.asJson
}