package kenbot.createandeditplugin

import java.io.File
import sbt._
import Project.Initialize
import Keys._
import CreateAndEditPlugin.DeclarationType


class CreateAndEditContext(
    args: Seq[String],
    sourceDir: File,
    testDir: File,
    config: CreateAndEditConfig)  {

  
  def createClassAndTestOrShowUsage(declType: DeclarationType) {
    if (args.length == 1) 
      createClassAndTest(declType, args(0))
    else 
      println("Usage: %s pkg.pkg2.MyThingy" format declType)
  }
  
  def editFilesOrShowUsage() {
    if (config.editorCommand.trim.isEmpty) {
      println("If you want to edit files direct from the sbt prompt, set createAndEditConfig ~= (_ editWith \"$MY_FAVORITE_EDITOR_CMD\")")
    }
    else if (args.length == 0) {
      println("Usage: edit file1 [file2..n] \n" + 
              "    or edit pkg.Class1 [pkg.Class2..n]")
    }
    else {
      println("Editing stuff")
      editFiles(args map transformClassNameToFile toList)
    }
  }
  
  
  private def createClassAndTest(declType: DeclarationType, fullClassName: String) {
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
    val sourceFileName = "%s/%s.scala"
    val formattedSourceFile = sourceDir / sourceFileName.format(path, className)
    val sourceFileContent = config.sourceFileTemplate.stripMargin.format(packageDecl, declType, className)
    val sourceFile = createFileContent(formattedSourceFile, sourceFileContent)

    val testFile: Option[File] = if (config.testFileName.nonEmpty && config.testFileName.nonEmpty) {
      val formattedTestFile = testDir / config.testFileName.format(path, className)
      val testFileContent = config.testFileTemplate.stripMargin.format(packageDecl, className)
      Some(createFileContent(formattedTestFile, testFileContent))
    } else None
    
    if (testFile.isEmpty) {
      println("To automatically create test files as well, set the test template using: \n" + 
          "set createAndEditConfig ~= (_ usingTestTemplate \"package %s; class %sTest {}")
    }
    
    if (canEdit) {
      editFiles(sourceFile :: testFile.toList)
    }
  }
  
  private def canEdit: Boolean = config.editorCommand.trim.nonEmpty && args.length > 0
  
  /**
   * Creates a file with the given name, and writes the given content to it.
   */
  private def createFileContent(file: File, content: String): File = {
    IO.touch(file)
    IO.write(file, content)
    
    println("Created " + file.getPath)
    file
  }
  
  private def editFiles(files: Seq[File]) {
    files filterNot (_.exists) foreach { f =>
      println("File not found: " + f)
    }
    val execArgs = (config.editorCommand +: files.map(_.getPath)).toArray
    println("Executing " + execArgs.mkString(" ") + "...")
    execArgs.toList!
  }
  
  private def transformClassNameToFile(fileOrFullClassName: String): File = {
    val isClassName = fileOrFullClassName.contains('.') && 
        !fileOrFullClassName.contains('/') && 
        !fileOrFullClassName.contains('\\')

    if (isClassName) {
      val fileName = fileOrFullClassName.replace('.', '/') + ".scala"
      findRelativeFile(fileName) getOrElse sourceDir / fileName
    }
    else {
      new File(fileOrFullClassName)
    }
  }
  
  private def sourceDirs = Seq(sourceDir, testDir)
  
  private def findRelativeFile(relativeFileName: String): Option[File] = {
    val validDirOpt = sourceDirs.find(_ / relativeFileName exists)
    validDirOpt.map(_ / relativeFileName)
  }
}
