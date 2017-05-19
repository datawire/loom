package io.datawire.loom.terraform

import com.natpryce.hamkrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import com.natpryce.hamkrest.assertion.assertThat
import java.nio.file.Files
import java.nio.file.Path

class TerraformTemplateTest {

  @get:Rule
  val tempStorage = TemporaryFolder()

  private val stringValue = TerraformString("I am the Walrus")
  private val listValue   = TerraformList("Hello", "darkness", "my", "old", "friend")
  private val mapValue    = TerraformMap(mapOf("foo" to "bar", "baz" to "bot"))

  @Test
  fun renderSimpleTemplate() {
    val backend = Backend(
        name = "s3",
        properties = mapOf(
            "bucket"  to "test_bucket",
            "key"     to "foo/bar/baz",
            "encrypt" to "true"
        )
    )

    val template = terraformTemplate(
        terraform = terraformBlock(backend),
        modules = listOf(
            Module(
                name = "foo",
                source = "/nonexistent/foo",
                variables = mapOf("stringValue" to stringValue, "listValue" to listValue, "mapValue" to mapValue)
            ),
            Module(
                name = "bar",
                source = "/nonexistent/bar",
                variables = mapOf("stringValue" to stringValue, "listValue" to listValue, "mapValue" to mapValue)
            )
        )
    )

    println(template.render())
  }

  @Test
  fun render_toPath_writesTemplateToPath() {
    val template = terraformTemplate(terraformBlock(Backend("fake-backend", mapOf("foo" to "bar"))))

    val outputPath = tempStorage.root.toPath().resolve("template.tf.json")
    template.render(outputPath)

    assertThat(outputPath, Matcher(this::fileExists))
  }

  private fun fileExists(path: Path) = Files.isRegularFile(path)
}