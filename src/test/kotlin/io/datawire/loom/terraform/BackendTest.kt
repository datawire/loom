package io.datawire.loom.terraform

import org.junit.Test

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import io.datawire.loom.core.Json


class BackendTest {

  private val json = Json()

  private val region = "us-east-2"
  private val bucket = "loom-terraform"
  private val key    = "terraform-state"

  @Test
  fun createS3Backend_returnValidS3Backend() {
    val backend = createS3Backend(region, bucket, key)

    assert.that(backend.name, equalTo("s3"))
    assert.that(backend.properties["region"], equalTo(region))
    assert.that(backend.properties["bucket"], equalTo(bucket))
    assert.that(backend.properties["key"], equalTo(key))
    assert.that(backend.properties["encrypt"], equalTo("true"))
  }

  @Test
  fun createS3Backend_withEncryptAsFalse_returnValidS3BackendWithEncryptAsFalse() {
    val backend = createS3Backend(region, bucket, key, encrypt = false)

    assert.that(backend.name, equalTo("s3"))
    assert.that(backend.properties["region"], equalTo(region))
    assert.that(backend.properties["bucket"], equalTo(bucket))
    assert.that(backend.properties["key"], equalTo(key))
    assert.that(backend.properties["encrypt"], equalTo("false"))
  }

  @Test
  fun createS3Backend_renderWithTemplateView_returnsJustPropertiesAndNotName() {
    val backend = createS3Backend(region, bucket, key)
    val json = json.writeUsingView<TemplateView>(backend)

    // read it back into a JsonNode which we can then inspect programmatically
    val jsonNode = this.json.toJsonNode(json)
    for (field in listOf("region", "bucket", "key", "encrypt")) {
      assert.that(jsonNode.has(field), equalTo(true))
    }

    assert.that(jsonNode.has("name"), equalTo(false))
  }
}