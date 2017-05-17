package io.datawire.loom.fabric


import io.datawire.loom.core.Yaml
import io.datawire.loom.kops.Kops
import io.datawire.loom.kops.cluster.ClusterContext
import io.datawire.loom.kops.cluster.ClusterContextLoader
import io.datawire.loom.kops.cluster.KubernetesClients
import io.fabric8.kubernetes.client.KubernetesClientException
import org.slf4j.LoggerFactory
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit


class KopsKubernetesClusterService(
    private val kops: (cluster: String) -> Kops
) : KubernetesClusterService {

  private val logger = LoggerFactory.getLogger(javaClass)

  override fun isAvailable(name: String): Boolean {
    return getClusterContext(name)?.let { ctx ->
      try {
        val client = KubernetesClients.newClient(ctx, connectTimeout = TimeUnit.SECONDS.toMillis(2).toInt())
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

  override fun exists(name: String): Boolean = kops(name).getCluster(name) != null

  override fun getByName(name: String): Cluster? {
    return if (exists(name)) {
      return Cluster(name, isAvailable(name))
    } else {
      null
    }
  }

  override fun getClusterContextByName(name: String) = kops(name).exportClusterContext(name)

  /**
   * Get a cluster context which is used to connect a Kubernetes client to a Kubernetes cluster. If the context cannot
   * be created then null is returned.
   *
   * @param name fully qualified name of the cluster to check.
   * @return the cluster context for the given name or null.
   */
  private fun getClusterContext(name: String): ClusterContext? {
    return kops(name).exportClusterContext(name)?.let {
      val loader = ClusterContextLoader(Yaml().mapper)
      loader.load(it, name)
    }
  }
}
