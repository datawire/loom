package io.datawire.loom.model

import io.datawire.loom.exception.LoomException
import io.datawire.loom.model.*


class ResourceNotExistsException(val reference: EntityReference,
                                 message: String? = null) : LoomException(message = message)

fun modelNotFound(id: String) = notFound(ModelReference(id))
fun notFound(ref: ModelReference) = ResourceNotExistsException(ref, "fabric model '${ref.id}' does not exist")

fun fabricNotFound(name: String) = notFound(FabricReference(name))
fun notFound(ref: FabricReference) = ResourceNotExistsException(ref, "fabric '${ref.id}' does not exist")

fun clusterNotFound(name: String) = notFound(ClusterReference(name))
fun notFound(ref: ClusterReference) = ResourceNotExistsException(ref, "cluster '${ref.id}' does not exist")
