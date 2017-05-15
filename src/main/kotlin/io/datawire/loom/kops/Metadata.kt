package io.datawire.loom.kops

import java.time.Instant


data class Metadata(
    val name: String,
    val creationTimestamp: Instant,
    val labels: Map<String, String>
)