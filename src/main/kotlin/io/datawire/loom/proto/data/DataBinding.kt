package io.datawire.loom.proto.data

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.convertValue
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

/**
 * Bind arbitrary JSON to String-keyed map.
 *
 * @param json the input JSON to use for binding.
 */
fun fromJsonToMap(json: String) = fromJson<Map<String, Any?>>(json)

/**
 * Bind an arbitrary String-keyed map into an actual instance of [T]. Extremely useful when you had to do validation
 * received data before binding could occur, for example, to ensure no null values were present.
 *
 * @param map the input map to use for binding.
 */
inline fun <reified T: Any> fromMap(map: Map<String, Any?>): T = JSON_MAPPER.convertValue<T>(map)

inline fun <reified T: Any> fromJson(data: String): T = fromJson(data, T::class)
inline fun <reified T: Any> fromYaml(data: String): T = fromYaml(data, T::class)

inline fun <reified T: Any> fromJson(path: Path): T = fromJson(Files.newBufferedReader(path).readText())
inline fun <reified T: Any> fromYaml(path: Path): T = fromYaml(Files.newBufferedReader(path).readText())

/**
 * Transform an object into JSON. Implements the [ResponseTransformer] contract for Sparkjava framework.
 *
 * @property pretty whether the JSON should be "pretty" printed (true) or not (false).
 */
class Jsonifier(private val pretty: Boolean = true) : ResponseTransformer {
    override fun render(model: Any?) = toJson(model, pretty)
}

/**
 * Transform an object into YAML. Implements the [ResponseTransformer] contract for Sparkjava framework.
 */
class Yamlifier() : ResponseTransformer {
    override fun render(model: Any?) = toYaml(model)
}
