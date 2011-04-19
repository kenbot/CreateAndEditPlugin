package au.kenbot.createandeditplugin
import sbt._
import java.io.File.{separator => /}

trait EditInVim extends CreateAndEditPlugin {
  this: Project with Exec with ScalaPaths =>

  def vimHome = / + "usr" + / + "bin"
  override def editorCommand = Some(vimHome + / + "vim")
}