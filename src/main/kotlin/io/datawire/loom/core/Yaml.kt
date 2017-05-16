package io.datawire.loom.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import java.nio.file.Path


class Yaml(val mapper: ObjectMapper = ObjectMapper(YAMLFactory())) {

  init {
    mapper.registerModules(KotlinModule(), Jdk8Module(), ParameterNamesModule(), JavaTimeModule())
  }

  fun write(any: Any): String = mapper.writeValueAsString(any)

  fun write(any: Any, outputFile: Path) = mapper.writeValue(outputFile.toFile(), any)

  inline fun <reified T: Any> read(text: String): T = mapper.readValue(text)

  inline fun <reified T: Any> read(path: Path): T = mapper.readValue(path.toFile())
}