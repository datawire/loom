package io.datawire.loom.terraform.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import io.datawire.loom.terraform.TerraformList
import io.datawire.loom.terraform.TerraformMap
import io.datawire.loom.terraform.TerraformString
import io.datawire.loom.terraform.TerraformValue

class TerraformValueSerializer : StdSerializer<TerraformValue<*>>(TerraformValue::class.java) {
  override fun serialize(tfValue: TerraformValue<*>, gen: JsonGenerator, provider: SerializerProvider) {
    when (tfValue) {
      is TerraformString -> gen.writeString(tfValue.value)
      is TerraformMap -> gen.writeObject(tfValue.value)
      is TerraformList -> {
        gen.writeStartArray()
        tfValue.value.forEach { gen.writeString(it) }
        gen.writeEndArray()
      }
    }
  }
}