package io.datawire.loom.kops

import io.datawire.loom.fabric.FabricSpec
import java.time.Instant


data class InstanceGroupConfig(val metadata: Metadata, val spec: InstanceGroupSpec) {
  val kind       = "InstanceGroup"
  val apiVersion = "kops/v1alpha2"
}

private fun FabricSpec.generateLabels() = mapOf("kops.k8s.io/cluster" to clusterDomain)

internal fun FabricSpec.createWorkerInstanceGroupConfigs(): List<InstanceGroupConfig> {
  val creationTime = Instant.now()

  return workerNodes.map { (name, type, count, zones) ->
    val metadata = Metadata(
        creationTimestamp = creationTime,
        name              = name,
        labels            = generateLabels()
    )

    val spec = InstanceGroupSpec(
        associatePublicIp = true,
        image             = "kope.io/k8s-1.5-debian-jessie-amd64-hvm-ebs-2017-01-09",
        machineType       = type,
        minSize           = count,
        maxSize           = count,
        role              = InstanceRole.NODE,
        subnets           = zones.toList()
    )

    InstanceGroupConfig(metadata, spec)
  }
}

internal fun FabricSpec.createMasterInstanceGroupConfigs(): List<InstanceGroupConfig> {
  val creationTime = Instant.now()

  return masterNodes.availabilityZones.map { zone ->
    val metadata = Metadata(
        creationTimestamp = creationTime,
        name              = "master-$zone",
        labels            = generateLabels()
    )

    val spec = InstanceGroupSpec(
        associatePublicIp = true,
        image             = "kope.io/k8s-1.5-debian-jessie-amd64-hvm-ebs-2017-01-09",
        machineType       = masterNodes.type,
        minSize           = 1,
        maxSize           = 1,
        role              = InstanceRole.MASTER,
        subnets           = listOf(zone)
    )

    InstanceGroupConfig(metadata, spec)
  }
}