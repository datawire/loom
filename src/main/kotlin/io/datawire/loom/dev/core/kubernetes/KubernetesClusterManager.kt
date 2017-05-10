package io.datawire.loom.dev.core.kubernetes

import io.datawire.loom.v1.Cluster
import io.datawire.loom.v1.ClusterConfig


interface KubernetesClusterManager {

  /**
   * Create a cluster if it does not exist otherwise do nothing.
   *
   * @param config a cluster configuration to use when creating the cluster.
   */
  fun create(config: ClusterConfig)

  /**
   * Delete a cluster if it exists otherwise do nothing.
   *
   * @param name the fully qualified name of the cluster.
   */
  fun delete(name: String)

  /**
   * Retrieve information about a cluster.
   *
   * @param name the fully qualified name of the cluster.
   */
  fun fetchCluster(name: String): Cluster?

  /**
   * Retrieve the Kubernetes cluster context configuration.
   *
   * @param name the fully qualified name of the cluster.
   */
  fun fetchClusterContext(name: String): String?
}