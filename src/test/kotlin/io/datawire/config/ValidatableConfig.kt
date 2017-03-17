package io.datawire.config

import io.datawire.vertx.Config
import io.vertx.kotlin.core.json.JsonObject
import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.NotBlank
import javax.validation.constraints.Min


class ValidatableConfig(@get:NotBlank
                        @get:Email
                        val email: String,

                        @get:Min(0)
                        val age: Int) : Config {

    companion object {
        val VALID_CONFIG  = JsonObject("email" to "phil@example.org", "age" to 27)
        val INVALID_CONFIG = JsonObject("email" to "!!phil_example.org", "age" to -1)
    }
}