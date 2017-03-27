package io.datawire.loom.fabric.kops

import io.datawire.loom.core.ExternalTool
import io.datawire.loom.core.ExternalToolExecutor
import io.datawire.loom.core.ExternalToolExecutorContext
import java.nio.file.*


class KopsTool(private val tool: ExternalTool,
               private val context: KopsToolContext) {

    fun getCluster(clusterName: String, detailed: Boolean = false): Map<String, Any?> {
        TODO("See KopsTool1")
    }

    fun deleteCluster(params: DeleteClusterParams): Boolean {
        val execCtx = prepareExecutionContext()
        val execCmd = kops("delete", "cluster", "--yes") + params.toCommandOptions()

        val result  = ExternalToolExecutor(execCmd, execCtx).execute()
        return when(result.exitValue) {
            0    -> true
            else -> false
        }
    }

    fun createCluster(params: CreateClusterParams): Boolean {
        val execCtx = prepareExecutionContext()
        val execCmd = kops("create", "cluster", "--yes", "--target=direct") + params.toCommandOptions()
        val result  = ExternalToolExecutor(execCmd, execCtx).execute()
        return when(result.exitValue) {
            0    -> true
            else -> false
        }
    }

    /**
     * Export Kubernetes context configuration (a.k.a: kubeconfig, kubecfg) for the requested cluster.
     *
     * @param clusterName the fully qualified (name + domain) for the cluster.
     * @return YAML formatted string containing Kubernetes config that can be consumed by kubectl.
     */
    fun exportKubernetesConfiguration(clusterName: String): String {
        val execCtx = prepareExecutionContext()
        val execCmd = kops("create", "export", "kubecfg", "--name=$clusterName")
        val result  = ExternalToolExecutor(execCmd, execCtx).execute()
        return when(result.exitValue) {
            0    -> Files.newBufferedReader(context.workspace.resolve(".kube/config")).readText()
            else -> throw RuntimeException("Unable to get kubeconfig for cluster $clusterName")
        }
    }

    private fun kops(vararg args: String) = (arrayOf(tool.executable) + args).toMutableList()

    private fun prepareExecutionContext(): ExternalToolExecutorContext {
        val variables = mutableMapOf<String, String>(
                "PATH" to System.getenv("PATH"),
                "HOME" to context.workspace.toAbsolutePath().toString(),
                "KOPS_STATE_STORE" to "s3://${context.stateStorageBucket}"
        )

        System.getenv("AWS_ACCESS_KEY_ID")?.let     { variables += Pair("AWS_ACCESS_KEY_ID", it) }
        System.getenv("AWS_SECRET_ACCESS_KEY")?.let { variables += Pair("AWS_SECRET_ACCESS_KEY", it) }
        System.getenv("AWS_DEFAULT_REGION")?.let    { variables += Pair("AWS_DEFAULT_REGION", it) }
        System.getenv("AWS_REGION")?.let            { variables += Pair("AWS_REGION", it) }

        return ExternalToolExecutorContext(context.workspace, variables)
    }
}
