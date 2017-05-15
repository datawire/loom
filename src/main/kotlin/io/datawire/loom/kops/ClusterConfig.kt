package io.datawire.loom.kops


data class ClusterConfig(
    val metadata: Metadata,
    val spec: ClusterSpec
)
