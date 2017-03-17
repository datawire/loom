package io.datawire.config

import io.datawire.vertx.Config
import io.vertx.kotlin.core.json.JsonObject


data class SimpleConfig(val name: String, val age: Int) : Config {

    companion object {
        val VALID_CONFIG   = JsonObject("name" to "Phil", "age" to 27)
        val INVALID_CONFIG = JsonObject("name" to "Phil", "age" to "Lombardi")
    }
}