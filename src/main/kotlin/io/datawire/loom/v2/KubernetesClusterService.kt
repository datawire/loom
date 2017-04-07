package io.datawire.loom.v2


import io.datawire.loom.cluster.ClusterContext
import io.datawire.loom.cluster.ClusterContextLoader
import io.datawire.loom.cluster.KubernetesClients
import io.datawire.loom.data.YAML_MAPPER
import io.datawire.loom.v2.kops.Kops
import io.fabric8.kubernetes.client.KubernetesClientException
import org.slf4j.LoggerFactory
import java.net.SocketTimeoutException
import java.nio.file.Files


class KubernetesClusterService(
    private val kops: (cluster: String) -> Kops
) : ClusterService {

  private val logger = LoggerFactory.getLogger(javaClass)

  /**
   * Create a cluster if it does not exist otherwise do nothing.
   */
  override fun create(config: ClusterConfig) {
    if (!exists(config.name)) {
      kops(config.name).createCluster(config)
    } else {
      logger.info("Cluster not created because it exists already (name: {})", config.name)
    }
  }

  /**
   * Delete a cluster if it exists otherwise do nothing.
   */
  override fun delete(name: String) {
    if (exists(name)) {
      kops(name).deleteCluster(name)
    } else {
      logger.info("Cluster not deleted because it does not exist (name: {})", name)
    }
  }

  /**
   * Checks to see if a cluster with the provided name is active. This is an expensive query that attempts to poke the
   * API endpoint on a known Kubernetes cluster to determine if the cluster is running.
   *
   * @param name fully qualified name of the cluster to check
   */
  override fun isAvailable(name: String): Boolean {
    return createClusterContext(name)?.let { ctx ->
      try {
        val client = KubernetesClients.newClient(ctx, connectTimeout = 2000)
        client.namespaces().withName("kube-system").get()
        true
      } catch(ex: KubernetesClientException) {
        when(ex.cause) {
          is SocketTimeoutException -> false
          else -> {
            logger.error("Unable to communicate with the cluster (name: {})", name, ex)
            throw ex
          }
        }
      }
    } ?: false
  }

  /**
   * Check to see if a cluster with the given name exists.
   */
  override fun exists(name: String): Boolean {
    val (status, _, _) = kops(name).getCluster(name)
    return status == 0
  }

  /**
   * Retrieve information about a cluster.
   */
  override fun getByName(name: String): Cluster? {
    return if (exists(name)) {
      val clusterInfo = kops(name).getCluster(name, true)
      return Cluster(name, isAvailable(name))
    } else {
      null
    }
  }

  private fun createClusterContext(name: String): ClusterContext? {
    val exported = kops(name).exportClusterContext(name)
    return when(exported.status) {
      0 -> {
        val loader = ClusterContextLoader(YAML_MAPPER)
        val context = Files.newBufferedReader(exported.workspace.resolve(".kube/config")).readText()
        loader.load(context, name)
      }
      else -> null
    }
  }
}
