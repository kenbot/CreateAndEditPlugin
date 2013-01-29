package kenbot.createandeditplugin

import java.io.File
import sbt._
import Project.Initialize
import Keys._

object CreateAndEditPlugin extends CreateAndEditPlugin {
    
  /**
   * The type of declaration: "class", "trait" or "object".
   */
  type DeclarationType = DeclarationType.Value
  object DeclarationType extends Enumeration {
    val Class = Value("class")
    val Trait = Value("trait")
    val Object = Value("object")
  }
  

  override def settings = createAndEditSettings
}

trait CreateAndEditPlugin extends Plugin {
  import CreateAndEditPlugin.DeclarationType
  
  lazy val classTask = declareTaskKey(DeclarationType.Class)
  lazy val traitTask = declareTaskKey(DeclarationType.Trait)
  lazy val objectTask = declareTaskKey(DeclarationType.Object)
  lazy val editTask = InputKey[Unit]("edit", 
      "Edits the given files in a text editor.  This will have no effect until you " + 
      "have specified the editor command: \n" + 
      "set createAndEditConfig ~= (_ editWith \"$MY_FAVORITE_EDITOR_CMD\")")
  
  lazy val createAndEditConfig = SettingKey[CreateAndEditConfig]("create-and-edit-config", 
      """
      |Configures the CreateAndEditPlugin with the following settings:
      |
      |- sourceFileTemplate
      |The template for creating source files with the class/trait/object commands. 
      |It is formatted with 3 %s arguments corresponding to the package name, 
      |"class"|"trait"|"object" and the class name.  
      |Leading whitespace will be removed from each line up to the first | character.
      |
      |- testFileTemplate
      |The template for creating test files with the class/trait/object commands. 
      |It is formatted with 2 %s arguments corresponding to the package name and the class name.
      |Leading whitespace will be removed from each line up to the first | character.
      |
      |- testFileName
      |A template for the name of test files to create with the class/trait/object commands. 
      |It is formatted with 2 %s arguments corresponding to path name, and the name of the
      |class that the test is being created for. "
      |
      |- editorCommand
      |Optionally defines the full path of an editor command, to which filenames can be passed for editing.
      |If this setting is empty, then files will not be edited.
      """.stripMargin)
  
  def createAndEditSettings = Seq(
    createAndEditConfig := CreateAndEditConfig.default,
    classTask <<= defineTask(_ createClassAndTestOrShowUsage DeclarationType.Class),
    traitTask <<= defineTask(_ createClassAndTestOrShowUsage DeclarationType.Trait),
    objectTask <<= defineTask(_ createClassAndTestOrShowUsage DeclarationType.Object),
    editTask <<= defineTask(_.editFilesOrShowUsage)
  )
  
  
  private def declareTaskKey(declType: DeclarationType): InputKey[Unit] = 
    InputKey[Unit](declType.toString, defaultDeclareEntityTaskDescription(declType))

  private def defaultDeclareEntityTaskDescription(declType: DeclarationType) = 
      "Declares a new %s with the given fully qualified name, ".format(declType) + 
      "a corresponding test class, and optionally loads both files into the chosen editor." + 'goobar
  
  private def defineTask[A](f: CreateAndEditContext => A): Initialize[InputTask[A]] = inputTask {  
      args => (args, scalaSource in Compile, scalaSource in Test, createAndEditConfig) map { 
        (args, mainSource, testSource, config) => 
                
        val context = new CreateAndEditContext(args, mainSource, testSource, config)
        f(context)
      }
  }

}
