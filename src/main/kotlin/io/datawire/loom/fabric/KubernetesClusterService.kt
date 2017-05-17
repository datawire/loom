package io.datawire.loom.fabric


interface KubernetesClusterService {

  /**
   * Check to see if a cluster with the given name exists.
   *
   * @param name fully-qualified name of the cluster.
   */
  fun exists(name: String): Boolean

  /**
   * Checks to see if a cluster with the provided name is active. This can be a very expensive query that may attempt
   * to poke the API endpoint on a Kubernetes cluster to determine if the cluster is running. Implementations may
   * perform a reasonable amount of caching to improve performance.
   *
   * @param name fully qualified name of the cluster to check.
   * @return a boolean indicating if the cluster is available (true = available, false = not available).
   */
  fun isAvailable(name: String): Boolean

  /**
   * Retrieve information about a cluster.
   *
   * @param name fully-qualified name of the cluster.
   */
  fun getByName(name: String): Cluster?

  /**
   * Retrieve the Kubernetes cluster context configuration.
   *
   * @param name fully-qualified name of the cluster.
   */
  fun getClusterContextByName(name: String): String?
}