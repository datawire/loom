package io.datawire.loom.model


sealed class EntityReference(val id: String)

class ModelReference(id: String): EntityReference(id) {
    constructor(name: String, version: Int) : this("$name-$version")
}

class FabricReference(name: String)  : EntityReference(name)
class ClusterReference(name: String) : EntityReference(name)