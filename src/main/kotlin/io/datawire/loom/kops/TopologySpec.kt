package io.datawire.loom.kops


data class TopologySpec(
    val dns: Map<String, String>,
    val masters: String,
    val nodes: String
)