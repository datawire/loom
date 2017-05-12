package io.datawire.loom.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import java.nio.file.Path
import kotlin.reflect.KClass


object Json {

  private val modules = listOf(KotlinModule(), Jdk8Module(), ParameterNamesModule(), JavaTimeModule())

  val mapper: ObjectMapper by lazy { ObjectMapper().registerModules(modules) }

  inline fun <reified T: Any> toJsonUsingView(any: Any?): String =
      writer(T::class).withDefaultPrettyPrinter().writeValueAsString(any)

  inline fun <reified T: Any> toJsonUsingView(any: Any?, outputFile: Path) =
      writer(T::class).withDefaultPrettyPrinter().writeValue(outputFile.toFile(), any)

  fun writer(view: KClass<*>): ObjectWriter {
    return mapper.writerWithView(view.java)
  }
}
