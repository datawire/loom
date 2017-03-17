package io.datawire.vertx.exception

import io.vertx.core.VertxException
import javax.validation.ConstraintViolation


class InvalidConfigException(
        message: String? = null,
        cause: Throwable? = null) : VertxException(message, cause) {

    constructor(cause: Throwable): this(cause.message, cause)

    constructor(violations: Collection<ConstraintViolation<*>>): this(buildMessage(violations))
}

private fun buildMessage(violations: Collection<ConstraintViolation<*>>): String {
    var msg = "Configuration validation failed.\n\n"
    violations.forEachIndexed { idx, v ->
        msg += "Constraint violation for ${v.rootBeanClass}['${v.propertyPath}']\n"
        msg += "\tViolation value: ${v.invalidValue}\n"
        msg += "\tViolation error: ${v.message}.\n"

        if (idx != violations.size - 1) {
            msg += "\n"
        }
    }

    return msg
}