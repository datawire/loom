package io.datawire.loom.kops

import io.datawire.loom.fabric.FabricSpec
import java.time.Instant


data class ClusterConfig(val metadata: Metadata, val spec: ClusterSpec) {
  val kind       = "Cluster"
  val apiVersion = "kops/v1alpha2"
}

internal fun FabricSpec.toClusterConfig(configBase: String): ClusterConfig {
  val mainEtcdCluster = EtcdCluster(
      "main", masterNodes.availabilityZones.map { EtcdMember(it.last().toString(), "master-$it", true) }
  )

  val zones = (masterNodes.availabilityZones) + workerNodes.flatMap { it.availabilityZones }.toSet()

  val metadata = Metadata(creationTimestamp = Instant.now(), name = clusterDomain, labels = emptyMap())
  val spec = ClusterSpec(
      api                 = mapOf("dns" to emptyMap()),
      authorization       = mapOf("alwaysAllow" to emptyMap()),
      channel             = "stable",
      cloudLabels         = emptyMap(),
      cloudProvider       = "aws",
      configBase          = "s3://$configBase/$clusterDomain",
      dnsZone             = clusterDomainZone,
      etcdClusters        = listOf(mainEtcdCluster, mainEtcdCluster.copy(name = "events")),
      kubernetesApiAccess = listOf("0.0.0.0/0"),
      kubernetesVersion   = "1.5.4",
      masterPublicName    = "api.$clusterDomain",
      networkCIDR         = clusterCidr,
      networking          = mapOf("kubenet" to emptyMap()),
      nonMasqueradeCIDR   = "100.64.0.0/10",
      sshAccess           = listOf("0.0.0.0/0"),
      subnets             = zones.mapIndexed { idx, zone -> SubnetSpec(zone, computeCidr(idx + 1, clusterCidr), SubnetType.PUBLIC, zone) },
      topology            = TopologySpec(
          dns     = mapOf("type" to "Public"),
          masters = "public",
          nodes   = "public"
      )
  )

  return ClusterConfig(metadata, spec)
}

private fun computeCidr(count: Int, networkCidr: String): String {
  val cidrInfo = org.apache.commons.net.util.SubnetUtils(networkCidr).info
  val addressParts = cidrInfo.address.split('.').map { it.toInt() }.toIntArray()

  addressParts[2] = 32 * count

  return addressParts.joinToString(".", postfix = "/19")
}
