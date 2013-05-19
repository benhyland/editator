import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "editator"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
      "joda-time" % "joda-time" % "2.2"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
