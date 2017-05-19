package io.datawire.loom.fabric

import io.datawire.loom.terraform.*


class TerraformService(private val models: InMemoryResourceModelDao) {

  fun generateTemplate(spec: FabricSpec): Template {
    // TODO: Multi-cluster support
    //
    // It seems plausible that at some point in the future a Fabric will need to be able to support multiple Kubernetes
    // clusters. To make that transition easier we will generate modules named after the cluster DNS name which must be
    // unique.

    val name = spec.clusterDomain.replace('.', '-')
    val kubernetes = Module(
        name   = "kubernetes_$name",
        source = "./kubernetes_$name"
    )

    val clusters = listOf(kubernetes)

    val nodeSecurityGroups = if (clusters.size == 1) {
      clusters[0].outputList("node_security_group_ids")
    } else {
      TerraformList("\${concat(${clusters.joinToString(separator = ", ") { it.outputRef("node_security_group_ids") } })")
    }

    val externalServicesNetwork = Module(
        name      = "external_services_network",
        source    = spec.resourcesNetwork.module,
        variables = mapOf(
            "name"                       to TerraformString("fabric-${spec.name}"),
            "cidr_block"                 to TerraformString(spec.resourcesNetwork.cidr),
            "kubernetes_security_groups" to nodeSecurityGroups
        )
    )

    val clusterPeeredWithResourcesNetwork = Module(
        name      = "external_services_network-${kubernetes.name}",
        source    = "github.com/datawire/loom//terraform/networking-peering-v1",
        variables = mapOf(
            "external_services_vpc"                       to externalServicesNetwork.outputString("vpc_id"),
            "external_services_vpc_external_route_table"  to externalServicesNetwork.outputString("external_route_table_id"),
            "external_services_vpc_internal_route_tables" to externalServicesNetwork.outputList("internal_route_tables_id"),

            "kubernetes_vpc"             to kubernetes.outputString("vpc_id"),
            "kubernetes_vpc_route_table" to kubernetes.outputString("route_table_id")
        )
    )

    val modules = listOf(
        kubernetes,
        externalServicesNetwork,
        clusterPeeredWithResourcesNetwork
    ) + generateCustomModules()

    return terraformTemplate(
        modules = modules,
        outputs = listOf(
            OutputReference("${kubernetes.name}_vpc_id", kubernetes.outputString("vpc_id")),
            OutputReference("external_services_vpc_id", externalServicesNetwork.outputRef("vpc_id"))
        )
    )
  }

  private fun generateCustomModules(): List<Module> = emptyList()
}
