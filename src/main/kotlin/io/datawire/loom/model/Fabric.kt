package io.datawire.loom.model


data class Fabric(
        val name              : String,
        val model             : String,
        val clusterName       : String?,
        val networkId         : String?,
        val networkCidr       : String?,
        val availabilityZones : List<String> = emptyList(),
        val status            : FabricStatus = FabricStatus.NOT_STARTED)
