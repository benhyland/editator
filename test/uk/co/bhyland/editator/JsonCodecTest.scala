package uk.co.bhyland.editator

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.co.bhyland.editator.messages.JsonCodec._
import uk.co.bhyland.editator.model.User
import uk.co.bhyland.editator.messages.ToggleJoinResponse
import uk.co.bhyland.editator.messages.RoomMembershipUpdate
import uk.co.bhyland.editator.messages.RoomMessageEvent
import org.joda.time.DateTime

class JsonCodecTest extends FunSuite with ShouldMatchers {

  test("decodeWithRoomKey on user without id should decode a key and user") {
    val in = """{"nick":"bob","id":"","key":"#room"}"""
    val parsed = decodeWithRoomKeyAs[User](in)
  
    parsed should be ('defined)
    parsed foreach { case (key, user) =>
      key should be ("#room")
      user.name should be ("bob")
      user.id should not be ('empty)
    }
  }
  
  test("decodeWithRoomKey on user without id or nick should decode a key and user") {
    val in1 = """{"key":"#room"}"""
    val parsed1 = decodeWithRoomKeyAs[User](in1)

    val in2 = """{"key":"#room","nick":""}"""
    val parsed2 = decodeWithRoomKeyAs[User](in1)
    
    checkParse(parsed1)
    checkParse(parsed2)
    
    def checkParse(parsed: Option[(String, User)]) = {
	  parsed should be ('defined)
	  parsed foreach { case (key, user) =>
	    key should be ("#room")
	    user.name should not be ('empty)
	    user.id should not be ('empty)
	  }
    }
  }
  
  test("ToggleJoinResponse should encode to correct json string") {
    val tjr = ToggleJoinResponse("#room", true, User("id", "name"))
    val json = tjr.json
    val expected = 
   """|{
      |  "roomKey" : "#room",
      |  "isJoined" : true,
      |  "user" : {
      |    "id" : "id",
      |    "nick" : "name"
      |  }
      |}""".stripMargin
      
    json.spaces2 should be (expected)
  }
  
  test("encodeWithMessageType on RoomMembershipUpdate should encode to correct json string") {
    val rmu = RoomMembershipUpdate(List(User("a", "1"), User("b", "2"), User("c", "3")))
    val json = encodeWithMessageTypeAs("blah", rmu)
    val expected =
   """|{
      |  "type" : "blah",
      |  "members" : [
      |    {
      |      "id" : "a",
      |      "nick" : "1"
      |    },
      |    {
      |      "id" : "b",
      |      "nick" : "2"
      |    },
      |    {
      |      "id" : "c",
      |      "nick" : "3"
      |    }
      |  ]
      |}""".stripMargin
      
    json.spaces2 should be (expected)
  }
  
  test("encodeWithMessageType on RoomMessageEvent should encode to correct json string") {
    val rme = RoomMessageEvent("key", "id", new DateTime(123456789L), "message")
    val json = encodeWithMessageTypeAs("blah", rme)
    val expected =
   """|{
      |  "type" : "blah",
      |  "from" : "id",
      |  "time" : "1970-01-02 10:17:36 +0000",
      |  "text" : "message"
      |}""".stripMargin
      
    json.spaces2 should be (expected)
  }
}