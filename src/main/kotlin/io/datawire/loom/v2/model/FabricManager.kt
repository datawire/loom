package io.datawire.loom.v2.model

import io.datawire.loom.v2.persistence.S3Persistence
import io.datawire.vertx.fromJson
import io.datawire.vertx.toJson


class FabricManager(private val s3: S3Persistence) {

    private val keyRoot = "fabrics"

    fun getFabric(name: String): Fabric {
        val data = s3.getObject("$keyRoot/$name")
        return fromJson<Fabric>(data)
    }

    fun listNames(): List<String> {
        return s3.listObjects(keyRoot)
    }

    fun putFabric(fabric: Fabric) {
        s3.writeTextObject("$keyRoot/${fabric.name}", toJson(fabric))
    }

    fun deleteFabric(name: String) {
        s3.deleteObject("$keyRoot/$name")
    }
}