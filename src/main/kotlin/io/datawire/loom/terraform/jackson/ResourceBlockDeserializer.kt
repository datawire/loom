package io.datawire.loom.terraform.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import io.datawire.loom.terraform.*

object ResourceBlockDeserializer : StdDeserializer<ResourceBlock>(ResourceBlock::class.java) {
    override fun deserialize(parser: JsonParser, ctx: DeserializationContext): ResourceBlock {
        val result = mutableMapOf<String, Map<String, Resource>>()
        val node = parser.codec.readTree<JsonNode>(parser)
        val resourcesNode = node.get("resource")

        for (type in resourcesNode.fieldNames()) {
            val resources = mutableListOf<Resource>()
            val resourcesOfType = resourcesNode.get(type)
            for ((name, data) in resourcesOfType.fields()) {

                val props = mutableMapOf<String, TerraformValue<*>>()
                for ((k,v ) in data.fields()) {
                    props.put(k, jsonNodeToTerraformValue(v) ?: throw JsonMappingException(parser, "Invalid Type"))
                }

                resources.add(Resource(type, name, props))
            }

            result.put(type, resources.associateBy { it.name } )
        }

        return ResourceBlock(result)
    }
}