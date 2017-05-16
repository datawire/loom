package io.datawire.loom.fabric

import java.time.Instant

data class FabricModel(
    val active           : Boolean = true,
    val domain           : String,
    val creationTime     : Instant?,
    val masterNodes      : NodeGroup,
    val name             : String,
    val resourcesNetwork : ResourcesNetwork,
    val region           : String,
    val sshPublicKey     : String,
    val workerNodes      : List<NodeGroup>
)