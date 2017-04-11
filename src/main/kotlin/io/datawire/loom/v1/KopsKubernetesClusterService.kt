package io.datawire.loom.v1


import io.datawire.loom.proto.aws.symlinkAwsConfig
import io.datawire.loom.proto.cluster.ClusterContext
import io.datawire.loom.proto.cluster.ClusterContextLoader
import io.datawire.loom.proto.cluster.KubernetesClients
import io.datawire.loom.proto.data.YAML_MAPPER
import io.datawire.loom.proto.exception.fabricNotExists
import io.datawire.loom.v1.kops.Kops
import io.fabric8.kubernetes.client.KubernetesClientException
import org.slf4j.LoggerFactory
import java.net.SocketTimeoutException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit


class KopsKubernetesClusterService(
    private val kops: (cluster: String) -> Kops
) : KubernetesClusterService {

  private val logger = LoggerFactory.getLogger(javaClass)

  override fun create(config: ClusterConfig) {
    if (!exists(config.name)) {
      kops(config.name).createCluster(config)
    } else {
      logger.info("Cluster not created because it exists already (name: {})", config.name)
    }
  }

  override fun delete(name: String) {
    if (exists(name)) {
      kops(name).deleteCluster(name)
    } else {
      logger.info("Cluster not deleted because it does not exist (name: {})", name)
    }
  }

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

  override fun exists(name: String): Boolean {
    val (status, _, _) = kops(name).getCluster(name)
    return status == 0
  }

  override fun getByName(name: String): Cluster? {
    return if (exists(name)) {
      val clusterInfo = kops(name).getCluster(name, true)
      return Cluster(name, isAvailable(name))
    } else {
      null
    }
  }

  override fun getClusterContextByName(name: String): String? {
    val exported = kops(name).exportClusterContext(name)
    return when(exported.status) {
      0    -> Files.newBufferedReader(exported.workspace.resolve(".kube/config")).readText()
      else -> null
    }
  }

  /**
   * Get a cluster context which is used to connect a Kubernetes client to a Kubernetes cluster. If the context cannot
   * be created then null is returned.
   *
   * @param name fully qualified name of the cluster to check.
   * @return the cluster context for the given name or null.
   */
  private fun getClusterContext(name: String): ClusterContext? {
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
