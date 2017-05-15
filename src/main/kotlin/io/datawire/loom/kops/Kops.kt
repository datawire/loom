package io.datawire.loom.kops

import io.datawire.loom.core.ExternalTool
import io.datawire.loom.core.Workspace
import java.nio.file.Path


class Kops(
    executable: Path,
    home: Path,
    stateStore: String,
    private val workspace: Workspace
) : ExternalTool(executable) {

  private val env = mapOf(
      "HOME"             to home.toString(),
      "KOPS_STATE_STORE" to stateStore
  )

  fun exportConfig(name: String): String {
    return ""
  }

  /**
   * Registers cluster configuration with the Kops state store. Register *DOES NOT* create the actual cloud resources
   * that run the cluster.
   */
  fun registerClusterConfig(config: CreateClusterConfig) {

  }

  /**
   * Unregister a cluster that is registered in the Kops state store. Unregister *DOES NOT* delete the actual cloud
   * resources that run the cluster.
   *
   * @param name the name of the cluster.
   */
  fun unregisterCluster(name: String) {
    val cmd = kops("delete", "cluster", "--unregister", "--name=$name")
    val res = execute(cmd, workspace.path, env)

    if (res.exitCode != 0) {
      throw RuntimeException("")
    }
  }

  fun version(): String {
    val result = execute(kops("version"), workspace.path, env)

    // version string = "Version X.Y.Z-$EXTRA_INFO (git-$COMMIT_HASH)"
    return result.output
        ?.substringAfter("Version ")
        ?.substringBefore(' ')
        ?: throw RuntimeException("Unable to retrieve version information from `$executableFile`.")
  }

  private fun kops(args: List<String>) = listOf(executableFile.toString()) + args

  private fun kops(vararg args: String) = kops(args.toList())
}