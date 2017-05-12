package io.datawire.loom.terraform

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer


@JsonDeserialize(using = TerraformValueDeserializer::class)
@JsonSerialize(using = TerraformValueSerializer::class)
sealed class TerraformValue<out T> {
  abstract val value: T
}

data class TerraformString(override val value: String): TerraformValue<String>()

data class TerraformList(override val value: List<String>): TerraformValue<List<String>>() {

  constructor(vararg items: String): this(items.toList())
}

data class TerraformMap(override val value: Map<String, String>) : TerraformValue<Map<String, String>>()

class TerraformValueSerializer : StdSerializer<TerraformValue<*>>(TerraformValue::class.java) {
  override fun serialize(tfValue: TerraformValue<*>, gen: JsonGenerator, provider: SerializerProvider) {
    when (tfValue) {
      is TerraformString -> gen.writeString(tfValue.value)
      is TerraformMap    -> gen.writeObject(tfValue.value)
      is TerraformList   -> {
        gen.writeStartArray()
        tfValue.value.forEach { gen.writeString(it) }
        gen.writeEndArray()
      }
    }
  }
}

class TerraformValueDeserializer : StdDeserializer<TerraformValue<*>>(TerraformValue::class.java) {

  override fun deserialize(parser: JsonParser, ctx: DeserializationContext): TerraformValue<*> {
    val node = parser.codec.readTree<JsonNode>(parser)
    return jsonNodeToTerraformValue(node) ?: throw JsonMappingException(parser, "Invalid Type")
  }

  private fun jsonNodeToMap(node: JsonNode): Map<String, String> {
    val result = mutableMapOf<String, String>()
    for ((k, v) in node.fields()) {
      result.put(k, v.textValue())
    }

    return result
  }
}

fun jsonNodeToTerraformValue(node: JsonNode): TerraformValue<*>? {
  return when {
    node.isTextual -> TerraformString(node.textValue())
    node.isNumber  -> TerraformString(node.numberValue().toString()) // terraform converts to string internally
    node.isArray   -> TerraformList(node.map { it.textValue() })
    node.isObject  -> TerraformMap(jsonNodeToMap(node))
    else           -> null
  }
}

private fun jsonNodeToMap(node: JsonNode): Map<String, String> {
  val result = mutableMapOf<String, String>()
  for ((k, v) in node.fields()) {
    result.put(k, v.textValue())
  }

  return result
}