package io.datawire.loom.v2


data class ClusterConfig(
    val name              : String,
    val availabilityZones : Set<String>,
    val channel           : String,
    val networkId         : String,
    val networkCidr       : String,
    val masterType        : String = "t2.small",
    val masterCount       : Int?,
    val nodeGroups        : List<ClusterNodeConfig> = listOf(ClusterNodeConfig("main", 1, "t2.nano")),
    val sshPublicKey      : String,
    val labels            : Map<String, String> = emptyMap()
)