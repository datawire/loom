package io.datawire.loom.kops


data class FabricSpecParameters(
    val name: String,
    val clusterCidr: String = "20.0.0.0/16"
)
