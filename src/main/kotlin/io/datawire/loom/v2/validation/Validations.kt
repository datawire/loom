package io.datawire.loom.v2.validation

import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory

sealed class ValidationResult

data class Errored(val field: String, val reason: String): ValidationResult()

interface ValidationCheck<in T: Any?> {
    fun check(field: String, value: Any?): Errored?
}

class NotNull : ValidationCheck<Any?> {
    override fun check(field: String, value: Any?) = value?.let { null } ?: Errored(field, "cannot be null")
}


class JsonValidator(private val map: Map<String, List<ValidationCheck<*>>>) {

    private val logger = LoggerFactory.getLogger(JsonValidator::class.java)

    fun validate(json: JsonObject): List<Errored> {
        return map.entries.fold(listOf()) { res, (field, checks) ->
            val candidate = json.getValue(field)
            res + checks.fold(listOf<Errored>()) { acc, vc -> vc.check(field, candidate)?.let { acc + it } ?: acc }
        }
    }
}

