package io.datawire.loom.api

import io.datawire.loom.fabric.Fabric
import io.datawire.loom.fabric.FabricModel

val FABRIC_MODEL_TYPE = jsonType<FabricModel>()
val FABRIC_TYPE = jsonType<Fabric>()

inline fun <reified T: Any> jsonType() = "application/vnd.loom.${T::class.simpleName}-v1+json"