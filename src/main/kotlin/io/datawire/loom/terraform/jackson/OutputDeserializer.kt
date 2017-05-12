package io.datawire.loom.terraform.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import io.datawire.loom.terraform.Output
import io.datawire.loom.terraform.jsonNodeToTerraformValue


object OutputDeserializer : StdDeserializer<Output>(Output::class.java) {
    override fun deserialize(parser: JsonParser, ctx: DeserializationContext): Output {
        val outputNode = parser.codec.readTree<JsonNode>(parser)
        val outputName = parser.parsingContext.currentName
        return Output(outputName, jsonNodeToTerraformValue(outputNode["value"]) ?: throw JsonMappingException(parser, "Invalid Type"))
    }
}