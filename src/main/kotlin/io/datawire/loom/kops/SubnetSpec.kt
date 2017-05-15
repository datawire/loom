package io.datawire.loom.kops


data class SubnetSpec(
    val name: String,
    val cidr: String,
    val type: SubnetType,
    val zone: String
)
