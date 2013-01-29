package kenbot.createandeditplugin

import java.io.File
import sbt._
import Project.Initialize
import Keys._
import CreateAndEditPlugin.DeclarationType


case class CreateAndEditConfig(
  sourceFileTemplate: String,
  testFileTemplate: String,
  testFileName: String,
  editorCommand: String) {
  
  def editWith(cmd: String) = copy(editorCommand = cmd)
  def usingSourceTemplate(template: String) = copy(sourceFileTemplate = template)
  def usingTestTemplate(template: String) = copy(testFileTemplate = template)
  def usingTestFileName(template: String) = copy(testFileName = template)
}
  

object CreateAndEditConfig {
  val default = CreateAndEditConfig(
      SourceFileTemplates.default,
      TestFileTemplates.default,
      TestFileNames.default, 
      EditorCommands.default)
}


object TestFileNames {
  val default = "%s/%sTest.scala"
  val xxxTestDotScala = "%s/%sTest.scala"
  val xxxSpecDotScala = "%s/%sSpec.scala"
}

object SourceFileTemplates {
  val default = """%s
      |
      |%s %s {
      |
      |}
  """
}

object TestFileTemplates {
   val default = ""
   val junit = """%s
      |import org.junit._
      |
      |class %sTest {
      |  @Test
      |  def test%2$s() {
      |    Assert.fail()
      |  }
      |}
    """
     
   val scalaTest = """%s
      |import org.scalatest._
      |import matchers._
      |
      |class %sSpec extends Spec with ShouldMatchers {
      |  describe("%2$s") {
      |    it("should") {
      |    
      |    }
      |  }
      |}
    """
}

object EditorCommands {

  val default = ""
  val eclipse = stripQuotes(System.getenv("ECLIPSE_HOME")) + "/eclipse.exe"
  val notepad_++ = stripQuotes(System.getenv("NPP_HOME")) + "/notepad++.exe"
  val vim = "/usr/bin/vim"
  
  /**
   * Strips quotes from around a string. Useful for cleaning 
   * directories stored in Windows environment variables.
   */
  private def stripQuotes(s: String): String = {
    if (s.length >= 2 && s.charAt(0) == '"' && s.charAt(s.length - 1) == '"')
      s.substring(1, s.length-1)
    else
      s
  }
}
