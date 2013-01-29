This is a <a href="https://github.com/sbt/sbt">simple-build-tool</a> plugin that lets you declare and edit Scala classes, 
traits and objects from the sbt command line, and automatically load them into your favourite editor.  This is useful if you are spending a lot of time on the command line.

# Usage
Put this in a .scala file in your <code>$PROJECT_BASE/project/project</code> directory:
<code>
```scala
import sbt._
object PluginDef extends Build {
   override def projects = Seq(root dependsOn createAndEditPlugin)
   def root = Project("plugins", file(".")) 
   def createAndEditPlugin = uri("git://github.com/kenbot/CreateAndEditPlugin.git")
}
```
</code>

 At the sbt prompt, declare classes, traits or objects:
<pre>
<code>
> class com.grocery.Potato
Created C:\Ken\Projects\SimpleGame\src\main\scala\com\grocery\Potato.scala
> trait com.grocery.Blah
Created C:\Ken\Projects\SimpleGame\src\main\scala\com\grocery\Blah.scala
> object com.stuff.Gumball
Created C:\Ken\Projects\SimpleGame\src\main\scala\com\stuff\Gumball.scala
</code>
</pre>

If you define create-and-edit-config with a test file template in your build.sbt:
<pre>
<code>
createAndEditConfig ~= (_ usingTestTemplate "package %s; class %sTest {}")
</code>
</pre>
then test files will be created as well:
<pre>
<code>
> trait com.grocery.Potato
Created C:\Ken\Projects\SimpleGame\src\main\scala\com\grocery\Potato.scala
Created C:\Ken\Projects\SimpleGame\src\test\scala\com\grocery\PotatoSpec.scala
</code>
</pre>
You can also configure the test file name pattern:
<pre>
<code>
createAndEditConfig ~= (_ usingTestFileName "%s/%sTest.scala")
</code>
</pre>
If you choose an editor command:
<pre>
<code>
createAndEditConfig ~= (_ editWith "~/bin/eclipse")
</code>
</pre>
then the class/trait/object commands will also launch the newly created file in an editor.

The <code>edit</code> command will edit files directly:
<pre>
<code>
> edit com.grocery.Potato
Executing ~/bin/eclipse /usr/apps/myproject/src/main/scala/com/grocery/Potato.scala
</code>
</pre>
