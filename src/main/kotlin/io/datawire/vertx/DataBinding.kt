package io.datawire.vertx

import com.fasterxml.jackson.module.kotlin.convertValue
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.Json
import io.vertx.core.json.Json.prettyMapper
import io.vertx.core.json.JsonObject
import java.nio.charset.Charset
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import javax.validation.Validation
import kotlin.reflect.full.memberProperties

object ValidatorFactory {

    private val validator by lazy { Validation.buildDefaultValidatorFactory().validator }

    fun validate(any: Any): Set<ConstraintViolation<*>> = validator.validate(any)
}

inline fun <reified T: Any> nullifyMissingFields(provided: Map<String, Any?>): Map<String, Any?> {
    return T::class.memberProperties.fold(provided.toMutableMap()) { m, it -> m.putIfAbsent(it.name, null); m }
}

fun toBuffer(data: String, charset: Charset = Charsets.UTF_8): Buffer = Buffer.buffer(data, charset.name())

fun validate(any: Any) {
    val violations = ValidatorFactory.validate(any)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations)
    }
}

fun toJson(any: Any): String = prettyMapper.writeValueAsString(any)

fun toJsonObject(any: Any): JsonObject = JsonObject.mapFrom(any)

fun <T: Any> fromJson(json: JsonObject, clazz: Class<T>, validate: Boolean = true): T {
    val bound = Json.mapper.convertValue(json.map, clazz)
    if (validate) {
        validate(bound)
    }

    return bound
}

inline fun <reified T: Any> fromJson(json: JsonObject, validate: Boolean = true): T {
    val bound = Json.mapper.convertValue<T>(json.map)
    if (validate) {
        validate(bound)
    }

    return bound
}

inline fun <reified T: Any> fromJson(string: String, validate: Boolean = true): T =
        fromJson(Buffer.buffer(string), validate)

inline fun <reified T: Any> fromJson(buffer: Buffer, validate: Boolean = true): T =
        fromJson(buffer.toJsonObject(), validate)
