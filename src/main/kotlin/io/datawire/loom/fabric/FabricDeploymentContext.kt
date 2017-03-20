package io.datawire.loom.fabric

import io.datawire.vertx.fromJson
import io.datawire.vertx.toJsonObject
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import java.time.Instant


data class FabricDeploymentContext(
        val name          : String,
        val specification : FabricSpec,
        val status        : FabricStatus,
        val startTime     : Instant? = null,
        val endTime       : Instant? = null) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun updateStatus(vertx: Vertx) {
        val deployments = vertx.sharedData().getLocalMap<String, JsonObject>("deployments")
        val json = toJsonObject(this)
        logger.trace("--- Generated JSON Begin ---\n{}", json)

        deployments.put(name, json)
        logger.debug("Fabric deployment record updated (name: {}, status: {})", name, status)
    }

    companion object {
        fun get(vertx: Vertx, name: String) {
            val deployments = vertx.sharedData().getLocalMap<String, JsonObject>("deployments")
            return fromJson(deployments[name])
        }
    }
}