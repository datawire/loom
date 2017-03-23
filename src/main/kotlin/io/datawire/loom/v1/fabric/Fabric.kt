package io.datawire.loom.v1.fabric


data class Fabric(
        val name   : String,
        val model  : String,
        val status : FabricStatus = FabricStatus.NOT_STARTED)
