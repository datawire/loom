package io.datawire.loom.fabric


sealed class NodeGroup(
    val name              : String,
    val type              : String,
    val count             : Int,
    val availabilityZones : Set<String>
)
