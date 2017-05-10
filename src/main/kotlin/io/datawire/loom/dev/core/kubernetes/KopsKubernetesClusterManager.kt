package io.datawire.loom.dev.core.kubernetes

import io.datawire.loom.v1.Cluster
import io.datawire.loom.v1.ClusterConfig


class KopsKubernetesClusterManager : KubernetesClusterManager {

  override fun create(config: ClusterConfig) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun delete(name: String) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun fetchCluster(name: String): Cluster? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun fetchClusterContext(name: String): String? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}