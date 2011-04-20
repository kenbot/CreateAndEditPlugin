package au.kenbot.createandeditplugin
import sbt._

trait EditInNotepad_++ extends CreateAndEditPlugin {
  this: Project with Exec with ScalaPaths =>

  def nppHome = stripQuotes(System.getenv("NPP_HOME"))
  override def editorCommand = Some(nppHome + "/notepad++.exe")
}