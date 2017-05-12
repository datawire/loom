package io.datawire.loom.terraform.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import io.datawire.loom.terraform.Backend

object BackendDeserializer : StdDeserializer<Backend>(Backend::class.java) {
    override fun deserialize(parser: JsonParser, ctx: DeserializationContext): Backend {
        val backendNode = parser.codec.readTree<JsonNode>(parser)
        val backendName = parser.parsingContext.currentName

        val props = mutableMapOf<String, String>()
        for ((k,v ) in backendNode.fields()) {
            props += Pair(k, v.textValue())
        }

        return Backend(backendName, props)
    }
}