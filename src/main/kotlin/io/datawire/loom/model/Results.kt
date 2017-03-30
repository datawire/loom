package io.datawire.loom.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer


@JsonSerialize(using = ResultsSerializer::class)
data class Results<out T>(val name: String, val items: List<T>)


private class ResultsSerializer : StdSerializer<Results<*>>(Results::class.java) {
    override fun serialize(value: Results<*>, gen: JsonGenerator, provider: SerializerProvider) {
        with(gen) {
            writeStartObject()
            writeArrayFieldStart(value.name)
            for (it in value.items) {
                writeObject(it)
            }
            writeEndArray()
            writeEndObject()
        }
    }
}