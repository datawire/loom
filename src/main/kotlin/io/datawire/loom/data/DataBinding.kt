package io.datawire.loom.data

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import spark.ResponseTransformer
import java.nio.file.Files
import java.nio.file.Path
import kotlin.reflect.KClass

private val DEFAULT_MODULES = listOf(KotlinModule(), Jdk8Module(), ParameterNamesModule(), JavaTimeModule())

val JSON_MAPPER: ObjectMapper = ObjectMapper().registerModules(DEFAULT_MODULES)
val YAML_MAPPER: ObjectMapper = ObjectMapper(YAMLFactory()).registerModules(DEFAULT_MODULES)

fun toJson(any: Any?, pretty: Boolean = true): String {
    return when {
        pretty -> JSON_MAPPER.writer().withDefaultPrettyPrinter().writeValueAsString(any)
        else   -> JSON_MAPPER.writeValueAsString(any)
    }
}

fun toYaml(any: Any?): String = YAML_MAPPER.writeValueAsString(any)

fun toJson(path: Path, any: Any?, pretty: Boolean = true) {
    return when {
        pretty -> JSON_MAPPER.writer().withDefaultPrettyPrinter().writeValue(path.toFile(), any)
        else   -> JSON_MAPPER.writeValue(path.toFile(), any)
    }
}

fun toYaml(path: Path, any: Any?) = YAML_MAPPER.writeValue(path.toFile(), any)

fun <T: Any> fromJson(data: String, clazz: KClass<T>): T = JSON_MAPPER.readValue(data, clazz.java)
fun <T: Any> fromYaml(data: String, clazz: KClass<T>): T = YAML_MAPPER.readValue(data, clazz.java)

inline fun <reified T: Any> fromJson(data: String): T = fromJson(data, T::class)
inline fun <reified T: Any> fromYaml(data: String): T = fromYaml(data, T::class)

inline fun <reified T: Any> fromJson(path: Path): T = fromJson(Files.newBufferedReader(path).readText())
inline fun <reified T: Any> fromYaml(path: Path): T = fromYaml(Files.newBufferedReader(path).readText())

class Jsonifier(private val pretty: Boolean = true) : ResponseTransformer {
    override fun render(model: Any?) = toJson(model, pretty)
}

class Yamlifier() : ResponseTransformer {
    override fun render(model: Any?) = toYaml(model)
}
