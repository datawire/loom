package io.datawire.loom.terraform.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import io.datawire.loom.terraform.Provider
import io.datawire.loom.terraform.TerraformValue
import io.datawire.loom.terraform.jsonNodeToTerraformValue


object ProviderDeserializer : StdDeserializer<Provider>(Provider::class.java) {
    override fun deserialize(parser: JsonParser, ctx: DeserializationContext): Provider {
        val providerNode = parser.codec.readTree<JsonNode>(parser)
        val providerName = parser.parsingContext.currentName

        val props = mutableMapOf<String, TerraformValue<*>>()
        for ((k,v ) in providerNode.fields()) {
            props.put(k, jsonNodeToTerraformValue(v) ?: throw JsonMappingException(parser, "Invalid Type"))
        }

        return Provider(providerName, props)
    }
}