package io.datawire.loom.v2.model

import io.datawire.loom.v2.persistence.S3Persistence
import io.datawire.vertx.fromJson
import io.datawire.vertx.toJson


class FabricModelManager(private val s3: S3Persistence) {

    private val keyRoot = "fabric-models"

    fun getFabricModel(name: String): FabricModel {
        val data = s3.getObject("$keyRoot/$name")
        return fromJson<FabricModel>(data)
    }

    fun listModelNames(): List<String> {
        return s3.listObjects(keyRoot)
    }

    fun putFabricModel(model: FabricModel) {
        s3.writeTextObject("$keyRoot/${model.name}", toJson(model))
    }

    fun deleteFabricModel(name: String) {
        s3.deleteObject("$keyRoot/$name")
    }
}

