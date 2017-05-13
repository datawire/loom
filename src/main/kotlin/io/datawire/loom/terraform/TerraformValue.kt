package io.datawire.loom.terraform

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.datawire.loom.terraform.jackson.TerraformValueDeserializer
import io.datawire.loom.terraform.jackson.TerraformValueSerializer


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