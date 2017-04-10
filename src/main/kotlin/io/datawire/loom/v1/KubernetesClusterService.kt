package io.datawire.loom.v1


interface KubernetesClusterService {

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
   * Checks to see if a cluster with the provided name is active. This is potentially a very expensive query that
   * attempts to poke the API endpoint on a Kubernetes cluster to determine if the cluster is running.
   *
   * @param name fully qualified name of the cluster to check.
   * @return a boolean indicating if the cluster is available (true = available, false = not available).
   */
  fun exists(name: String): Boolean

  /**
   * Check to see if a cluster with the given name exists.
   */
  fun isAvailable(name: String): Boolean

  /**
   * Retrieve information about a cluster.
   */
  fun getByName(name: String): Cluster?

  /**
   * Retrieve the Kubernetes cluster context configuration.
   */
  fun getClusterContextByName(name: String): String?
}