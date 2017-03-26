package io.datawire.loom.model


data class Fabric(
        val name   : String,
        val model  : String,
        val status : FabricStatus = FabricStatus.NOT_STARTED)
