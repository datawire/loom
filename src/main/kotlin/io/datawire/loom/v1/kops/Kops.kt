package io.datawire.loom.v1.kops

import io.datawire.loom.proto.aws.symlinkAwsConfig
import io.datawire.loom.proto.core.ExternalToolExecutor
import io.datawire.loom.proto.core.ExternalToolExecutorContext
import io.datawire.loom.v1.ClusterConfig
import io.datawire.loom.v1.ProcessResult
import io.datawire.loom.v1.toProcessResult
import java.nio.file.Files
import java.nio.file.Path


class Kops(
    private val executable   : String = "kops",
    private val stateStorage : String,
    private val workspace    : Path
) {

  fun getCluster(name: String, detailed: Boolean = false): ProcessResult {
    val ctx = buildExecutionContext()
    val cmd = kops("get", "cluster", "--name=$name", "--output=json")

    if (detailed) {
      cmd += "--full"
    }

    exportClusterContext(name)

    val result = ExternalToolExecutor(cmd, ctx).execute()
    return toProcessResult(ctx.workspace, result)
  }

  fun createCluster(config: ClusterConfig): ProcessResult {
    val ctx = buildExecutionContext()

    val sshPublicKey = Files.write(ctx.workspace.resolve("ssh_public_key"), config.sshPublicKey.toByteArray())

    val cmd = kops("create", "cluster", "--yes", "--target=direct") +
        toCreateCommandOptions(config) +
        "--ssh-public-key=${sshPublicKey.toAbsolutePath()}"

    val result = ExternalToolExecutor(cmd, ctx).execute()
    return toProcessResult(ctx.workspace, result)
  }

  fun deleteCluster(name: String): ProcessResult {
    val ctx = buildExecutionContext()
    val cmd = kops("delete", "cluster", "name=$name", "--yes")

    val result = ExternalToolExecutor(cmd, ctx).execute()
    return toProcessResult(ctx.workspace, result)
  }

  fun exportClusterContext(name: String): ProcessResult {
    val ctx = buildExecutionContext()
    return if (!Files.exists(ctx.workspace.resolve(".kube/config"))) {
      val cmd = kops("export", "kubecfg", "--name=$name")
      val result = ExternalToolExecutor(cmd, ctx).execute()
      toProcessResult(ctx.workspace, result)
    } else {
      ProcessResult(0, ctx.workspace, null)
    }
  }

  /**
   * Construct a Kubernetes Ops ("kops") command.
   */
  private fun kops(vararg args: String) = (arrayOf(executable) + args).toMutableList()

  /**
   * Convert a [ClusterConfig] into the correct arguments for consumption by `kops create cluster ...`
   */
  private fun toCreateCommandOptions(config: ClusterConfig): List<String> {
    val mainNodeGroup = config.nodeGroups[0]

    val res = mutableListOf(
        "--associate-public-ip=true",
        "--cloud=aws",
        "--channel=${config.channel}",
        "--dns=public",
        "--master-size=${config.masterType}",
        "--name=${config.name}",
        "--network-cidr=${config.networkCidr}",
        "--node-size=${mainNodeGroup.nodeType}",
        "--node-count=${mainNodeGroup.nodeCount}",
        "--topology=public",
        "--vpc=${config.networkId}",
        "--zones=${config.availabilityZones.joinToString(",")}"
    )

    if (config.labels.isNotEmpty()) {
      res += "--cloud-labels=${config.labels.entries.joinToString(",", prefix = "\"", postfix = "\"")}"
    }

    config.masterCount?.let {
      res += "--master-count=$it"
    }

    return res
  }

  private fun buildExecutionContext(): ExternalToolExecutorContext {
    val variables = mutableMapOf<String, String>(
        "PATH"             to System.getenv("PATH"),
        "HOME"             to workspace.toAbsolutePath().toString(),
        "KOPS_STATE_STORE" to "s3://$stateStorage"
    )

    System.getenv("AWS_ACCESS_KEY_ID")?.let     { variables += Pair("AWS_ACCESS_KEY_ID", it) }
    System.getenv("AWS_SECRET_ACCESS_KEY")?.let { variables += Pair("AWS_SECRET_ACCESS_KEY", it) }
    System.getenv("AWS_DEFAULT_REGION")?.let    { variables += Pair("AWS_DEFAULT_REGION", it) }
    System.getenv("AWS_REGION")?.let            { variables += Pair("AWS_REGION", it) }

    try {
      symlinkAwsConfig(workspace.resolve(".aws"))
    } catch (ex: Throwable) {
      ex.printStackTrace()
    }

    return ExternalToolExecutorContext(workspace, variables)
  }
}