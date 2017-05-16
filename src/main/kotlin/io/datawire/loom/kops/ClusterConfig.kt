package io.datawire.loom.kops

import io.datawire.loom.fabric.FabricSpec
import java.time.Instant


data class ClusterConfig(val metadata: Metadata, val spec: ClusterSpec) {
  val kind       = "Cluster"
  val apiVersion = "kops/v1alpha2"
}

internal fun FabricSpec.toClusterConfig(): ClusterConfig {
  val metadata = Metadata(creationTimestamp = Instant.now(), name = clusterDomain, labels = emptyMap())
  val spec = ClusterSpec(
      api                 = mapOf("dns" to emptyMap()),
      authorization       = mapOf("alwaysAllow" to emptyMap()),
      channel             = "stable",
      cloudLabels         = emptyMap(),
      cloudProvider       = "aws",
      configBase          = "s3://",
      dnsZone             = "",
      etcdClusters        = emptyList(),
      kubernetesApiAccess = listOf("0.0.0.0/0"),
      kubernetesVersion   = "1.5.4",
      masterPublicName    = "api.$clusterDomain",
      networkCIDR         = clusterCidr,
      networking          = mapOf("kubenet" to emptyMap()),
      nonMasqueradeCIDR   = "100.64.0.0/10",
      sshAccess           = listOf("0.0.0.0/0"),
      subnets             = emptyList(),
      topology            = TopologySpec(
          dns     = mapOf("type" to "public"),
          masters = "Public",
          nodes   = "Public"
      )
  )

  return ClusterConfig(metadata, spec)
}
