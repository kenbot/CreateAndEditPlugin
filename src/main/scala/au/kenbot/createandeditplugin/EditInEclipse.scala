package au.kenbot.createandeditplugin
import sbt._
import java.io.File.{separator => /}

trait EditInEclipse extends CreateAndEditPlugin {
  this: Project with Exec with ScalaPaths  =>
  
  def eclipseHome = stripQuotes(System.getenv("ECLIPSE_HOME"))
  override def editorCommand = Some(eclipseHome + "/eclipse.exe")
}