package io.datawire.loom.kops

import io.datawire.loom.core.ExternalTool
import io.datawire.loom.core.Yaml
import io.datawire.loom.core.resolveExecutable
import io.datawire.loom.fabric.FabricWorkspace
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class Kops2(
    executable: Path,
    private val workspace: FabricWorkspace,
    private val stateStore: String
) : ExternalTool(executable) {

  private val env = workspace.environment + mapOf(
      "PATH"             to System.getenv("PATH"),
      "KOPS_STATE_STORE" to "s3://$stateStore"
  )

  companion object {

    private val kopsExecutable = resolveExecutable(
        name = "kops",
        searchPaths = setOf(
            "/bin",
            "/usr/local/bin",
            "/usr/bin",
            "${System.getProperty("user.home")}/bin"
        ).map { Paths.get(it) }.toSet()
    )

    fun newKops(workspace: FabricWorkspace, stateStore: String) = Kops2(kopsExecutable, workspace, stateStore)
  }

  fun updateCluster(name: String) {
    val cmd = kops("update", "cluster",
        "--name=$name",
        "--target=terraform",
        "--out=${workspace.resolve("terraform/kubernetes_$name")}")

    val res = execute(cmd, workspace.resolve("kops"), env)
    if (res.exitCode != 0) {
      throw RuntimeException("Failed to create SSH public key")
    }
  }

  /**
   * Registers cluster configuration with the Kops state store. Register *DOES NOT* create the actual cloud resources
   * that run the cluster.
   */
  fun createCluster(config: ClusterConfig) {
    val path = workspace.resolve("kops").resolve("${config.metadata.name}.yaml")
    Yaml().write(config, path)

    val cmd = kops("create", "-f", path.toString())
    val res = execute(cmd, workspace.resolve("kops"), env)

    if (res.exitCode != 0) {
      throw RuntimeException("Failed to create Cluster Config")
    }
  }

  fun createInstanceGroup(config: InstanceGroupConfig) {
    val path = workspace.resolve("kops").resolve("${config.metadata.name}.yaml")
    Yaml().write(config, path)

    val cmd = kops("create", "-f", path.toString())
    val res = execute(cmd, workspace.resolve("kops"), env)

    if (res.exitCode != 0) {
      throw RuntimeException("Failed to create Cluster Instance Group ${config.metadata}")
    }
  }

  fun createSshPublicKeySecret(name: String, key: String) {
    val publicKey = Files.write(workspace.resolve("kops").resolve("cluster.pubkey"), key.toByteArray())
    val cmd = kops("create", "secret", "--name=$name", "sshpublickey", "admin", "-i", publicKey.toAbsolutePath().toString())
    val res = execute(cmd, workspace.resolve("kops"), env)

    if (res.exitCode != 0) {
      throw RuntimeException("Failed to create SSH public key")
    }
  }

  fun exportClusterContext(name: String): String? {
    val contextConfig = workspace.home.resolve(".kube/config")

    return when {
      Files.isRegularFile(contextConfig) -> Files.newBufferedReader(contextConfig).readText()
      else -> {
        val cmd = kops("export", "kubecfg", "--name=$name")
        val res = execute(cmd, workspace.resolve("kops"), env)
        res.output
      }
    }
  }

//  fun getCluster(name: String, detailed: Boolean = false): String? {
//    val cmd = kops("get", "cluster", "--name=$name", "--outputRef=json")
//
////    if (detailed) {
////      cmd + "--full"
////    }
//
//    exportClusterContext(name)
//    val res = execute(cmd, home, env)
//    return when(res.exitCode) {
//      0    -> res.output
//      else -> null
//    }
//  }

  /**
   * Unregister a cluster that is registered in the Kops state store. Unregister *DOES NOT* delete the actual cloud
   * resources that run the cluster.
   *
   * @param name the name of the cluster.
   */
  fun unregisterCluster(name: String) {
    val cmd = kops("delete", "cluster", "--unregister", "--name=$name")
    val res = execute(cmd, workspace.resolve("kops"), env)

    if (res.exitCode != 0) {
      throw RuntimeException("")
    }
  }

  fun version(): String {
    val result = execute(kops("version"), workspace.resolve("kops"), env)

    // version string = "Version X.Y.Z-$EXTRA_INFO (git-$COMMIT_HASH)"
    return result.output
        ?.substringAfter("Version ")
        ?.substringBefore(' ')
        ?: throw RuntimeException("Unable to retrieve version information from `$executableFile`.")
  }

  private fun kops(args: List<String>) = listOf(executableFile.toString()) + args

  private fun kops(vararg args: String) = kops(args.toList())
}