package io.datawire.loom.terraform

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

object ModuleDeserializer : StdDeserializer<Module>(Module::class.java) {
    override fun deserialize(parser: JsonParser, ctx: DeserializationContext): Module {
        val providerNode = parser.codec.readTree<JsonNode>(parser)
        val providerName = parser.parsingContext.currentName

        val props = mutableMapOf<String, TerraformValue<*>>()
        for ((k,v ) in providerNode.fields()) {
            props.put(k, jsonNodeToTerraformValue(v) ?: throw JsonMappingException(parser, "Invalid Type"))
        }

        return Module(providerName, (props["source"] as TerraformString).value, (props - "source"))
    }
}