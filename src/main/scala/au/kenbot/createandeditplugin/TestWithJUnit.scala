package au.kenbot.createandeditplugin
import sbt._

trait TestWithJUnit extends CreateAndEditPlugin {
  this: Project with Exec with ScalaPaths =>
  
  override def testFileName: Option[String] = Some("%s/%sTest.scala")
  override def testFileTemplate = Some("""
    |%s
    |import org.junit._
    |
    |class %sTest {
    |  @Test
    |  def test%2$s() {
    |    Assert.fail()
    |  }
    |}
  """)
}