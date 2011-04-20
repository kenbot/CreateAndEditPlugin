package au.kenbot.createandeditplugin
import sbt._

trait TestWithScalaTest extends CreateAndEditPlugin {
  this: Project with Exec with ScalaPaths =>
  
  override def testFileName: Option[String] = Some("%s/%sSpec.scala")
  override def testFileTemplate = Some("""
    |%s
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
  """)
}