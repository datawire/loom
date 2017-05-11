package io.datawire.loom.proto.model


data class Fabric(
        val name              : String,
        val model             : String,
        val clusterName       : String?,
        val networkId         : String?,
        val networkCidr       : String = "30.0.0.0/16",
        val availabilityZones : List<String> = emptyList(),
        val status            : FabricStatus = FabricStatus.NOT_STARTED)
