package uk.co.bhyland.editator

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import uk.co.bhyland.editator.messages.JsonCodec._
import uk.co.bhyland.editator.model.User
import uk.co.bhyland.editator.messages.ToggleJoinResponse
import uk.co.bhyland.editator.messages.RoomMembershipUpdate

class JsonCodecTest extends FunSuite with ShouldMatchers {

  test("decodeWithRoomKey on user without id should decode a key and user") {
    val in = """{"nick":"bob","id":"","key":"#room"}"""
    val parsed = decodeWithRoomKey[User](in)
  
    parsed should be ('defined)
    parsed foreach { case (key, user) =>
      key should be ("#room")
      user.name should be ("bob")
      user.id should not be ('empty)
    }
  }
  
  test("decodeWithRoomKey on user without id or nick should decode a key and user") {
    val in1 = """{"key":"#room"}"""
    val parsed1 = decodeWithRoomKey[User](in1)

    val in2 = """{"key":"#room","nick":""}"""
    val parsed2 = decodeWithRoomKey[User](in1)
    
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
    val rmu = RoomMembershipUpdate(List("a", "b", "c"))
    val json = encodeWithMessageType("blah", rmu)
    val expected =
   """|{
      |  "type" : "blah",
      |  "nicks" : [
      |    "a",
      |    "b",
      |    "c"
      |  ]
      |}""".stripMargin
      
    json.spaces2 should be (expected)
  }
}