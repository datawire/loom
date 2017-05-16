package io.datawire.loom.fabric

import java.time.Instant


data class FabricModel(
    val active           : Boolean = true,
    val domain           : String,
    val creationTime     : Instant?,
    val model            : String,
    val masterNodes      : NodeGroup,
    val name             : String,
    val resourcesNetwork : ResourcesNetwork,
    val workerNodes      : List<NodeGroup>
)