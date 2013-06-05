package uk.co.bhyland.editator.messages

import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import org.joda.time.DateTime
import uk.co.bhyland.editator.model.User

trait AsJson {
  def asJson : JsValue
}

/** marks output for the client which goes asynchronously via websocket */
sealed trait EditatorOutput extends AsJson

case class RoomMembershipUpdate(nicks: List[String]) extends EditatorOutput {
  override def asJson = toJson(Map(
    "type" -> toJson("memberUpdate"),
    "members" -> toJson(nicks)          
  ))
}

case class RoomMessageEvent(from: String, timestamp: DateTime, message: String) extends EditatorOutput {
  override def asJson = toJson(Map(
    "type" -> toJson("roomMessage"),
    "message" -> toJson(Map(
      "from" -> toJson(from),
      "time" -> toJson(timestamp.toString("YYYY-MM-DDTHH:mm:ss Z")),
      "text" -> toJson(message)
    ))
  ))
}

case class SyncEvent(patch: String, checksum: String) extends EditatorOutput {
  override def asJson = toJson(Map(
    "type" -> toJson("sync"),
    "patch" -> toJson(patch),
    "check" -> toJson(checksum)
  ))
}

// response messages still go to the client as json
// but are synchronous responses rather than websocket events

case class RoomListUpdate(rooms: List[String]) extends AsJson {
  override def asJson = toJson(Map(
    "rooms" -> toJson(rooms)          
  ))
}

case class ToggleJoinResponse(roomKey: String, isJoined: Boolean, user: User) extends AsJson {
  override def asJson = toJson(Map(
    "roomKey" -> toJson(roomKey),
    "isJoined" -> toJson(isJoined),
    "user" -> toJson(Map(
      "id" -> toJson(user.id),
      "nick" -> toJson(user.name)
    ))
  ))
}

// ??
case class FullSyncResponse(content: String) extends AsJson {
  override def asJson = toJson(Map(
    "type" -> toJson("fullSync"),
    "doc" -> toJson(content)
  ))
}