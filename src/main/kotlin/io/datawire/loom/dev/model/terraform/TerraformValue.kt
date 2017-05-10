package io.datawire.loom.dev.model.terraform

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

sealed class TfValue<out T> {
  abstract val value: T
}

class TerraformString(override val value: String): TfValue<String>()
class TerraformList(override val value: List<String>): TfValue<List<String>>()
class TerraformMap(override val value: Map<String, String>) : TfValue<Map<String, String>>()

class TerraformValueSerializer : StdSerializer<TfValue<*>>(TfValue::class.java) {

  override fun serialize(value: TfValue<*>, gen: JsonGenerator, provider: SerializerProvider) {
    when (value) {
      is TerraformString -> gen.writeString(value.value)
      is TerraformList -> {
        gen.writeStartArray()
        value.value.forEach { gen.writeString(it) }
        gen.writeEndArray()
      }
      is TerraformMap -> {
        gen.writeObject(value.value)
      }
    }
  }
}