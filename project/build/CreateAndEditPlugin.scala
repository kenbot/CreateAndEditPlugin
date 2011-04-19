import sbt._

class CreateAndEditPluginProject(info: ProjectInfo) extends PluginProject(info) {
  lazy val scalaTest = "org.scalatest" % "scalatest" % "1.1" % "test->default"
}