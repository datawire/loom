package io.datawire.loom.proto.exception

import io.datawire.loom.proto.model.*


class ResourceNotExistsException(val reference: EntityReference,
                                 message: String? = null) : LoomException(message = message)

fun modelNotExists(id: String) = notFound(ModelReference(id))
fun notFound(ref: ModelReference) = ResourceNotExistsException(ref, "fabric model '${ref.id}' does not exist")

fun fabricNotExists(name: String) = notFound(FabricReference(name))
fun notFound(ref: FabricReference) = ResourceNotExistsException(ref, "fabric '${ref.id}' does not exist")

fun clusterNotExists(name: String) = notFound(ClusterReference(name))
fun notFound(ref: ClusterReference) = ResourceNotExistsException(ref, "cluster '${ref.id}' does not exist")
