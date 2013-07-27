package uk.co.bhyland.editator.messages

import argonaut._
import Argonaut._
import uk.co.bhyland.editator.model.User
import org.joda.time.DateTime

object JsonCodec {

  implicit class AsPlayJsValue(json: Json) {
    def forPlay = play.api.libs.json.Json.parse(json.nospaces)
  }

  implicit val dateTimeEncode = EncodeJson((d: DateTime) => jString(d.toString("YYYY-MM-DD HH:mm:ss Z")))
  
  // TODO: see if there is still a prepend bug?
  implicit val userCodec = CodecJson[User](
    (user: User) => ("nick" := user.name) ->: ("id" := user.id) ->: jEmptyObject,
    c => {
      val name = (c --\ "nick").as[String]
      val id = (c --\ "id").as[String]
      DecodeResult.ok(User(id.toOption, name.toOption))
    })
  
  implicit val roomMembershipUpdateEncode = jencode1L((rmu: RoomMembershipUpdate) => (rmu.nicks))("members")
  implicit val roomMessageEventEncode = jencode3L((rme: RoomMessageEvent) => (rme.from, rme.timestamp, rme.message))("from", "time", "text")  
  implicit val syncEventEncode = jencode2L((se: SyncEvent) => (se.patch, se.checksum))("patch", "check")
  
  implicit val roomListUpdateEncode = jencode1L((rlu: RoomListUpdate) => (rlu.rooms))("rooms")
  implicit val toggleJoinResponseEncode = jencode3L((tjr: ToggleJoinResponse) => (tjr.roomKey, tjr.isJoined, tjr.user))("roomKey", "isJoined", "user")  
  
  implicit val talkDecode = jdecode3L((roomKey: String, user: User, message: String) => Talk(roomKey, user, message))("key", "user", "blah")
  
  def encodeWithMessageTypeAs[A](messageType: String, a: A)(implicit encode: EncodeJson[A]) : Json = {
    jSingleObject("type", messageType.asJson).deepmerge(a.asJson) 
  }

  val keyDecode = DecodeJson(c => (c --\ "key").as[String])
  def decodeWithRoomKeyAs[A](text: String)(implicit decode: DecodeJson[A]) : Option[(String, A)] = {
    val withKeyDecode = keyDecode &&& decode
    text.decodeOption(withKeyDecode)
  }
  
  def decodeAs[A](text: String)(implicit decode: DecodeJson[A]) : Option[A] = {
    text.decodeOption(decode)
  }
}