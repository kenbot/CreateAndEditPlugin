package au.kenbot.createandeditplugin

import java.io.File
import sbt._

object CreateAndEditPlugin {
  
  /**
   * The type of declaration: "class", "trait" or "object".
   */
  object DeclarationType extends Enumeration {
    val Class = Value("class")
    val Trait = Value("trait")
    val Object = Value("object")
  }

  def defaultDeclareEntityTaskDescription(declType: DeclarationType.Value) = 
      "Declares a new %s with the given fully qualified name, ".format(declType) + 
      "a corresponding test class, and optionally loads both files into the chosen editor."
      
  val EditTaskDescription = "Edits the given files in a text editor."

}

/**
*  SBT Plugin that allows easy creation of class files with accompanying tests.
*/
trait CreateAndEditPlugin {
  this: Project with Exec with ScalaPaths =>
  
  import CreateAndEditPlugin._

  lazy val `class` = declareEntityTask(DeclarationType.Class)
  lazy val `trait` = declareEntityTask(DeclarationType.Trait)
  lazy val `object` = declareEntityTask(DeclarationType.Object)

  /**
  *  Defines a MethodTask which accepts a fully qualified class name, and delegates to 
  *  createClassAndTest() to create the source file, and optionally create a test and load in an editor.
  */
  def declareEntityTask(declType: DeclarationType.Value) = task {args => 
    if (args.length == 1) 
      createClassAndTest(declType, args(0))
    else 
      task {Some("Usage: %s <ClassName>" format declType)}
  } describedAs defaultDeclareEntityTaskDescription(declType)

  /**
   * Creates a file with the given name, and writes the given content to it.
   */
  def createFileContent(path: String, content: String): File = {
    val file = new java.io.File(path)
    FileUtilities.touch(file, log)
    FileUtilities.write(file, content, log)
    
    log.info("Created " + file.getPath())
    file
  }
  
  /**
   * Given a class, trait or object declaration, create a class with the given fully-qualified name.    
   * If testFileName and testFileTemplate are supplied, then create a test file as well.  If editor details
   * are supplied, then load the new files in the editor.
   */
  def createClassAndTest(declType: DeclarationType.Value, fullClassName: String): Task = {
    val lastDot = fullClassName.lastIndexOf('.')
    val pkg = lastDot match {
      case -1 => ""
      case n => fullClassName.substring(0, n)
    }
    val className = lastDot match {
      case -1 => fullClassName
      case n => fullClassName.substring(n+1, fullClassName.length)
    }
    val path = pkg.replace('.', '/')
    val packageDecl = if (path.length > 0) "package " + pkg else ""
    
    val formattedSourceFile = sourceDirectoryForCreation + '/' + sourceFileName.format(path, className)
    val sourceFileContent = sourceFileTemplate.stripMargin.format(packageDecl, declType, className)
    val sourceFile = createFileContent(formattedSourceFile, sourceFileContent)

    val testFile = for (fname <- testFileName; templ <- testFileTemplate) yield {
      val formattedTestFile = testDirectoryForCreation + '/' + fname.format(path, className)
      val testFileContent = templ.stripMargin.format(packageDecl, className)
      createFileContent(formattedTestFile, testFileContent)
    }
    
    editTask(sourceFile :: testFile.toList)
  }
  
  /**
   * Of the available source directories, select one as "the" source directory to create new files in.
   */
  def sourceDirectoryForCreation: String = mainSourceRoots.getPaths.toList.head.toString

  /**
   * Of the available test directories, select one as "the" test directory to create new files in.
   */
  def testDirectoryForCreation: String = testSourceRoots.getPaths.toList.head.toString
  
  /**
   *  The full path and name of the source file to be created.  This string is formatted with the command 
   * <code>format(path, className)</code>, where "path" is the package name with file-separators (/ or \) 
   * instead of dots, and "className" is the simple name of the class to be created.
   * <p>
   * For example, if <code>class com.foo.Bar</code> was entered at the SBT command line, then 
   * <code>override def sourceFileName = "%s/%s.scala"</code> would yield "com/foo/Bar.scala".
   */
  def sourceFileName = "%s/%s.scala"
  
  /**
   *  The content of the source file to be created.  The content will be formatted with the command 
   * <code>format(packageDecl, declType, className).stripMargin</code>, where "packageDecl" is the full package declaration
   * at the top of the file ("" if no package is used), "declType" is one of "class", "trait" or "object", and 
   * "className" is the simple name of the class to be created.
   * <p>
   * For example, if <code>class com.foo.Bar</code> was entered at the SBT command line, then <br/>
   * <code>
   *  override def sourceFileTemplate = """ <br/>
   *  &nbsp;&nbsp;|%s <br/>
   *  &nbsp;&nbsp;|%s %s { <br/>
   *  &nbsp;&nbsp;| <br/>
   *  &nbsp;&nbsp;|} <br/>
   * """
   * </code> <br/> 
   * would result in <br/>
   * <code>
   * package com.foo <br/>
   * class Bar { <br/>
   * <br/>
   * }
   * </code>
   */
  def sourceFileTemplate = """%s
      |
      |%s %s {
      |
      |}
  """

  /**
   * The full path and name of the test file to be created.  This string is formatted with the command 
   * <code>format(path, className)</code>, where "path" is the package name with file-separators (/ or \) 
   * instead of dots, and "className" is the simple name of the class that the test is being created for.
   * <p>
   * For example, if <code>class com.foo.Bar</code> was entered at the SBT command line, then 
   * <code>override def testFileName = Some("%s/%sTest.scala")</code> would yield "com/foo/BarTest.scala".
   * <p>
   * Returning <code>None</code> from this method will result in no test file being generated.
   */
  def testFileName: Option[String] = None
  
  
  /**
   *  The content of the test file to be created.  The content will be formatted with the command 
   * <code>format(packageDecl, className).stripMargin</code>, where "packageDecl" is the full package declaration
   * at the top of the file ("" if no package is used), and "className" is the simple name of the class that the test is being created for.
   * <p>
   * For example, if <code>class com.foo.Bar</code> was entered at the SBT command line, then <br/>
   * <code>
   *  override def testFileTemplate = Some(""" <br/>
   *  &nbsp;&nbsp;|%s <br/>
   *  &nbsp;&nbsp;|import org.junit._ <br/>
   *  &nbsp;&nbsp;|class %sTest { <br/>
   *  &nbsp;&nbsp;| <br/>
   *  &nbsp;&nbsp;|} <br/>
   * """)
   * </code> <br/> 
   * would result in <br/>
   * <code>
   * package com.foo <br/>
   * import org.junit._ <br/>
   * class BarTest { <br/>
   * <br/>
   * }
   * </code>
   * <p>
   * Returning <code>None</code> from this method will result in no test file being generated.
   */
  def testFileTemplate: Option[String] = None
      
  /**
   * Optionally defines the full path of an editor command, to which filenames can be passed for editing.
   * If <code>None</code> is returned from the method, then files will not be edited.
   */
  def editorCommand: Option[String] = None
  
  /**
   * Strips quotes from around a string. Useful for cleaning directories stored in Windows environment variables.
   */
  def stripQuotes(s: String): String = 
    if (s.length >= 2 && s.charAt(0) == '"' && s.charAt(s.length - 1) == '"') 
      s.substring(1, s.length-1)
    else 
      s

  
  /**
   * Defines a MethodTask which accepts one or more relative path-names of files, 
   * which are delegated to the <code>editTask()</code> method.  If a fully-qualified class name is given, 
   * it will attempt to load an equivalent matching file.  For example, com.foo.Bar would be interpreted as com/foo/Bar.scala.
   */
  lazy val edit = task {args =>
  
    def transformClassNameToFile(fileOrFullClassName: String): File = {
      val isClassName = fileOrFullClassName.contains('.') && 
          !fileOrFullClassName.contains('/') && 
          !fileOrFullClassName.contains('\\')

      if (isClassName) {
        val fileName = fileOrFullClassName.replace('.', '/') + ".scala"
        findRelativeFile(fileName) getOrElse new File(sourceDirectoryForCreation + '/' + fileName)
      }
      else {
        new File(fileOrFullClassName)
      }
    }


    if (args.length > 0) {
      editTask(args map transformClassNameToFile toList)
    }
    else {
      task {Some("Usage: edit file1 [file2..n]")}
    }
  } describedAs EditTaskDescription 

  private def findRelativeFile(relativeFileName: String): Option[File] = {
    val dirsToCheck = (mainSourceRoots +++ testSourceRoots).getPaths
    val validDirOpt = dirsToCheck.find(d => new File(d + '/' + relativeFileName).exists)
    validDirOpt.map(d => new File(d + '/' + relativeFileName))
  }
  
  /**
   * Edits the given files, by sending them to the shell command defined by <code>editorCommand</code>.
   */
  def editTask(files: List[File]) = task {
    for (ec <- editorCommand) {
      files filter (!_.exists) foreach {f =>
        log.info("File not found: " + f)
      }
      val execArgs = (ec :: files.map(_.getPath)).toArray
      log.info("Executing " + execArgs.mkString(" ") + "...")
      exec(execArgs).run
    }
    None
  }
}
