package io.datawire.loom.v1

import io.datawire.loom.proto.data.AwsS3Dao
import io.datawire.loom.proto.exception.notFound
import io.datawire.loom.proto.model.ClusterReference
import io.datawire.loom.proto.model.FabricReference


class FabricController(
    private val persistence        : AwsS3Dao<io.datawire.loom.proto.model.Fabric>,
    private val kubernetesClusters : KubernetesClusterService)
{

  fun deleteCluster(fabricName: String) {
    val fabric = getFabric(fabricName)
    fabric.clusterName?.let { kubernetesClusters.delete(it) }
  }

  fun getCluster(fabricName: String): Cluster {
    val fabric = getFabric(fabricName)
    val clusterName = fabric.clusterName!!
    return kubernetesClusters.getByName(clusterName) ?: throw notFound(ClusterReference(clusterName))
  }

  fun getClusterContext(fabricName: String): String? {
    val fabric = getFabric(fabricName)
    return kubernetesClusters.getClusterContextByName(fabric.clusterName!!)
  }

  private fun getFabric(name: String) = persistence.get(name) ?: throw notFound(FabricReference(name))
}