package io.datawire.loom.terraform

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.datawire.loom.core.Json
import io.datawire.loom.terraform.jackson.TerraformValueDeserializer
import org.junit.Test
import org.assertj.core.api.Assertions.*


class TerraformValueDeserializerTest {

  data class TestContainer(
      @JsonDeserialize(using = TerraformValueDeserializer::class)
      val stringValue : TerraformValue<*>,

      @JsonDeserialize(using = TerraformValueDeserializer::class)
      val numberValue: TerraformValue<*>,

      @JsonDeserialize(using = TerraformValueDeserializer::class)
      val listValue   : TerraformValue<*>,

      @JsonDeserialize(using = TerraformValueDeserializer::class)
      val mapValue    : TerraformValue<*>
  )

  @Test
  fun deserialize_usingValidValues_returnsProperlyDeserializedObject() {
    val json = """{
      "stringValue": "foobar",
      "numberValue": 42.31,
      "listValue": ["hello", "darkness", "my", "old", "friend"],
      "mapValue": {
        "foo": "bar",
        "baz": "bot"
      }
    }"""

    // verify the types are correct
    val deserialized = Json().read<TestContainer>(json)
    assertThat(deserialized.stringValue).isInstanceOf(TerraformString::class.java)
    assertThat(deserialized.numberValue).isInstanceOf(TerraformString::class.java)
    assertThat(deserialized.listValue).isInstanceOf(TerraformList::class.java)
    assertThat(deserialized.mapValue).isInstanceOf(TerraformMap::class.java)

    // verify the content is correct
    assertThat((deserialized.stringValue as TerraformString).value).isEqualTo("foobar")
    assertThat((deserialized.numberValue as TerraformString).value).isEqualTo("42.31")
    assertThat((deserialized.listValue as TerraformList).value).isEqualTo(listOf("hello", "darkness", "my", "old", "friend"))
    assertThat((deserialized.mapValue as TerraformMap).value).isEqualTo(mapOf("foo" to "bar", "baz" to "bot"))

  }
}