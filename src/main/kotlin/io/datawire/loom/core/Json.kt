package io.datawire.loom.core

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import java.nio.file.Path
import kotlin.reflect.KClass


class Json(val mapper: ObjectMapper = ObjectMapper(JsonFactory())) {

  init {
    mapper.registerModules(KotlinModule(), Jdk8Module(), ParameterNamesModule(), JavaTimeModule())
  }

  fun writer(view: KClass<*>): ObjectWriter {
    return mapper.writerWithView(view.java)
  }

  inline fun <reified T: Any> writeUsingView(any: Any?): String =
      writer(T::class).withDefaultPrettyPrinter().writeValueAsString(any)

  inline fun <reified T: Any> writeUsingView(any: Any?, outputFile: Path) =
      writer(T::class).withDefaultPrettyPrinter().writeValue(outputFile.toFile(), any)

  fun write(any: Any?): String = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(any)

  fun write(any: Any?, outputFile: Path) = mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile.toFile(), any)

  inline fun <reified T: Any> read(text: String): T = mapper.readValue(text)

  inline fun <reified T: Any> read(path: Path): T = mapper.readValue(path.toFile())

  inline fun <reified T: Any> read(node: JsonNode): T = mapper.treeToValue(node)

  fun toJsonNode(text: String): JsonNode = mapper.readTree(text)

}