package io.datawire.loom.kops


data class InstanceGroupSpec(
    val associatePublicIp: Boolean,
    val image: String,
    val machineType: String,
    val minSize: Int,
    val maxSize: Int,
    val role: InstanceRole,
    val subnets: List<String>
)
