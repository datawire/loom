package io.datawire.loom.v2

import io.datawire.loom.data.AwsS3Dao
import io.datawire.loom.exception.notFound
import io.datawire.loom.model.ClusterReference
import io.datawire.loom.model.FabricReference


class FabricController(
    private val persistence : AwsS3Dao<io.datawire.loom.model.Fabric>,
    private val clusters    : ClusterService
) {

  fun getCluster(fabricName: String): Cluster {
    val fabric = persistence.get(fabricName) ?: throw notFound(FabricReference(fabricName))

    // TODO: cluster name probably should not be nullable
    val clusterName = fabric.clusterName!!
    return clusters.getByName(clusterName) ?: throw notFound(ClusterReference(clusterName))
  }
}