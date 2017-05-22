package io.datawire.loom.terraform.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import io.datawire.loom.terraform.*

class ResourceBlockSerializer : StdSerializer<ResourceBlock>(ResourceBlock::class.java) {

  override fun serialize(block: ResourceBlock, gen: JsonGenerator, provider: SerializerProvider) {
    with(gen) {
      // block: {
      writeStartObject()

      for((type, resources) in block.resources) {
        writeFieldName(type)
        writeStartObject()
        for (r in resources) {
          writeFieldName(r.value.name)
          writeObject(r.value.properties)
        }
        writeEndObject()
      }

      writeEndObject()
    }
//
//
//    with(gen) {
//      writeStartObject()
//      writeFieldName(block.name)
//      writeStartObject()
//      for ((name, value) in block.properties) {
//        gen.writeFieldName(name)
//        when (value) {
//          is TerraformString -> gen.writeString(value.value)
//          is TerraformMap    -> gen.writeObject(value.value)
//          is TerraformList   -> {
//            gen.writeStartArray()
//            value.value.forEach { gen.writeString(it) }
//            gen.writeEndArray()
//          }
//        }
//      }
//      writeEndObject()
//      writeEndObject()
//    }
  }
}