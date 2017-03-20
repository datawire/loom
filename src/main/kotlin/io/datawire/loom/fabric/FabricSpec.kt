package io.datawire.loom.fabric


data class FabricSpec(
        val name: String,
        val modelId: String,
        val model: FabricModel? = null,
        val vpcId: String? = null,
        val vpcCidr: String? = null,
        val zones: List<String> = emptyList()
)