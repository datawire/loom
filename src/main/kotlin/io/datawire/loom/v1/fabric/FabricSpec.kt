package io.datawire.loom.v1.fabric


data class FabricSpec(
        val name: String,
        val modelId: String,
        val model: io.datawire.loom.v2.model.FabricModel? = null,
        val vpcId: String? = null,
        val vpcCidr: String? = null,
        val zones: List<String> = emptyList()
)