package io.datawire.loom.fabric

import java.time.Instant


data class FabricSpec(
    val clusterCidr       : String,
    val clusterDomain     : String,
    val clusterDomainZone : String,
    val creationTime      : java.time.Instant?,
    val model             : String,
    val masterNodes       : NodeGroup,
    val name              : String,
    val region            : String,
    val resourcesNetwork  : ExternalServicesNetworkSpec,
    val sshPublicKey      : String,
    val workerNodes       : List<NodeGroup>,
    val resources         : Map<String, ResourceSpec>
)

fun assembleFabricSpec(model: FabricModel, config: FabricConfig) = FabricSpec(
    clusterCidr       = config.clusterCidr,
    clusterDomain     = "${config.name}.${model.domain}",
    clusterDomainZone = model.domain,
    creationTime      = Instant.now(),
    model             = model.name,
    masterNodes       = model.masterNodes,
    name              = config.name,
    region            = model.region,
    resourcesNetwork  = assemble(model.externalServicesNetwork, ExternalServicesNetworkConfig(config.resourcesCidr)),
    sshPublicKey      = model.sshPublicKey,
    workerNodes       = model.workerNodes,
    resources         = emptyMap()
)
