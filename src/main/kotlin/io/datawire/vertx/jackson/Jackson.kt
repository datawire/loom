package io.datawire.vertx.jackson

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule

val DEFAULT_MODULES = setOf(KotlinModule(), Jdk8Module(), ParameterNamesModule(), JavaTimeModule())

fun configureMapper(mapper: ObjectMapper, modules: Set<Module>) {
    mapper.apply {
        for (m in modules) {
            registerModule(m)
        }
    }
}