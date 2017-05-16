package io.datawire.loom.fabric


data class NodeGroup(
    val name              : String,
    val type              : String,
    val count             : Int,
    val availabilityZones : Set<String>
)
