import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "editator"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
      "joda-time" % "joda-time" % "2.2",
      "io.argonaut" %% "argonaut" % "6.0-RC1",
      "org.scalatest" %% "scalatest" % "1.9.1" % "test"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
