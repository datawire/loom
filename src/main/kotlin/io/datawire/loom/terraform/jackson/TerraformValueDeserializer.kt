package io.datawire.loom.terraform.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import io.datawire.loom.terraform.TerraformValue
import io.datawire.loom.terraform.jsonNodeToTerraformValue

class TerraformValueDeserializer : StdDeserializer<TerraformValue<*>>(TerraformValue::class.java) {

  override fun deserialize(parser: JsonParser, ctx: DeserializationContext): TerraformValue<*> {
    val node = parser.codec.readTree<JsonNode>(parser)
    return jsonNodeToTerraformValue(node) ?: throw JsonMappingException(parser, "Invalid Type")
  }
}