package io.datawire.loom.fabric.kops

import io.datawire.loom.core.ExternalTool
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.ProcessResult
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.io.Closeable
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes


class KopsTool(private val tool: ExternalTool,
               private val context: KopsToolContext) {

    private class Executor(
            val command: List<String>,
            _environment: Map<String, String>) : AutoCloseable, Closeable {

        private val logger = LoggerFactory.getLogger(javaClass)

        val home: Path = Files.createTempDirectory("kops-")
        private val environment: Map<String, String>

        init {
            val realHome = Paths.get(System.getProperty("user.home"))

            val variables = mutableMapOf<String, String>("HOME" to home.toAbsolutePath().toString())
            System.getenv("AWS_ACCESS_KEY_ID")?.let     { variables += Pair("AWS_ACCESS_KEY_ID", it) }
            System.getenv("AWS_SECRET_ACCESS_KEY")?.let { variables += Pair("AWS_SECRET_ACCESS_KEY", it) }
            System.getenv("AWS_DEFAULT_REGION")?.let    { variables += Pair("AWS_DEFAULT_REGION", it) }
            System.getenv("AWS_REGION")?.let            { variables += Pair("AWS_REGION", it) }

            if (Files.exists(realHome.resolve(".aws"))) {
                val realAwsConfig   = realHome.resolve(".aws")
                val linkedAwsConfig = home.resolve(".aws")

                logger.debug("AWS configuration directory found, linking {} -> {}", linkedAwsConfig, realAwsConfig)
                Files.createSymbolicLink(linkedAwsConfig, realAwsConfig)
            }

            environment = (variables + _environment)
        }

        fun execute(): ProcessResult {
            val pe = ProcessExecutor().apply {
                command(this@Executor.command)
                directory(this@Executor.home.toFile())
                environment(this@Executor.environment)
                redirectOutput(Slf4jStream.ofCaller().asInfo())
                readOutput(true)
            }

            return pe.execute()
        }

        override fun close() {
            Files.walkFileTree(home, object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
                    Files.delete(file)
                    return FileVisitResult.CONTINUE
                }

                override fun postVisitDirectory(dir: Path?, exc: IOException?): FileVisitResult {
                    Files.delete(dir)
                    return FileVisitResult.CONTINUE
                }
            })

            Files.deleteIfExists(home)
        }
    }

    fun getCluster(clusterName: String, detailed: Boolean = false): Map<String, Any?> {
        TODO("See KopsTool1")
    }

    fun deleteCluster(clusterName: String) {
        val cmd = listOf(tool.executable, "delete", "cluster", "--name=$clusterName")

        return Executor(cmd, mapOf("KOPS_STATE_STORE" to "s3://${context.stateStorageBucket}")).use {
            val result = it.execute()
            if (result.exitValue == 0) {

            } else {
                throw RuntimeException("Unable to get kubeconfig for cluster $clusterName")
            }
        }
    }

    fun createCluster(params: CreateClusterParams) {
        val cmd = listOf(tool.executable, "create", "cluster", "--yes", "--target=direct") + params.toCommandOptions()
        return Executor(cmd, mapOf("KOPS_STATE_STORE" to "s3://${context.stateStorageBucket}")).use {
            val result = it.execute()
            if (result.exitValue != 0) {
                throw RuntimeException("Unable to create cluster ${params.clusterName}\n\n${result.outputUTF8()}")
            }
        }
    }

    /**
     * Export Kubernetes context configuration (a.k.a: kubeconfig, kubecfg) for the requested cluster.
     *
     * @param clusterName the fully qualified (name + domain) for the cluster.
     * @return YAML formatted string containing Kubernetes config that can be consumed by kubectl.
     */
    fun exportKubernetesConfiguration(clusterName: String): String {
        val cmd = listOf(tool.executable, "export", "kubecfg", "--name=$clusterName")
        return Executor(cmd, mapOf("KOPS_STATE_STORE" to "s3://${context.stateStorageBucket}")).use {
            val result = it.execute()
            if (result.exitValue == 0) {
                Files.newBufferedReader(it.home.resolve(".kube/config")).readText()
            } else {
                throw RuntimeException("Unable to get kubeconfig for cluster $clusterName")
            }
        }
    }
}
