package io.datawire.loom.v1.fabric

import io.datawire.loom.v1.core.Workspace
import io.datawire.loom.v1.exception.AlreadyExistsException
import io.datawire.loom.v1.exception.NotFoundException
import io.vertx.core.file.FileSystemException


class FileFabricModelStore(private val workspace: Workspace) {

    private val contentRoot = "fabric-models/"

    fun exists(id: String) = workspace.fileExists("$contentRoot/$id.json")

    fun get(id: String): FabricModel {
        return try {
            workspace.readJsonFileBlocking("$contentRoot/$id.json")
        } catch (fse: FileSystemException) {
            if (fse.cause is java.nio.file.NoSuchFileException) {
                throw NotFoundException()
            } else {
                throw fse
            }
        }
    }

    fun delete(id: String) {
        try {
            workspace.delete("$contentRoot/$id.json")
        } catch (fse: FileSystemException) {
            if (fse.cause is NoSuchFileException) { /* File is deleted already */ }
        }
    }

    fun put(model: FabricModel) {
        if (!exists(model.name)) {
            workspace.writeJsonFileBlocking("$contentRoot/${model.id}.json", model)
        } else {
            throw AlreadyExistsException()
        }
    }

    fun list(): List<FabricModel> {
        return workspace.listFiles(contentRoot).map { workspace.readJsonFileBlocking<FabricModel>(it) }
    }
}