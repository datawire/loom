package io.datawire.loom.v2.kops


import io.datawire.loom.v2.config.ExternalTool
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.ProcessResult
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*


class KopsTool(private val tool: ExternalTool, private val ctx: KopsContext): AutoCloseable, Closeable {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val environment = prepare()

    data class RuntimeEnvironment(val path: Path, val environment: Map<String, String>) : AutoCloseable, Closeable {
        override fun close() {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach { it.delete() }

            Files.deleteIfExists(path)
        }
    }

    override fun close() = environment.close()

    private fun prepare(): RuntimeEnvironment {
        val realHome = Paths.get(System.getProperty("user.home"))
        val tempHome = Files.createTempDirectory("fabric-")

        val variables = mutableMapOf<String, String>("HOME" to tempHome.toAbsolutePath().toString())
        System.getenv("AWS_ACCESS_KEY_ID")?.let     { variables += Pair("AWS_ACCESS_KEY_ID", it) }
        System.getenv("AWS_SECRET_ACCESS_KEY")?.let { variables += Pair("AWS_SECRET_ACCESS_KEY", it) }
        System.getenv("AWS_DEFAULT_REGION")?.let    { variables += Pair("AWS_DEFAULT_REGION", it) }
        System.getenv("AWS_REGION")?.let            { variables += Pair("AWS_REGION", it) }

        if (Files.exists(realHome.resolve(".aws"))) {
            val realAwsConfig   = realHome.resolve(".aws")
            val linkedAwsConfig = tempHome.resolve(".aws")

            logger.debug("AWS configuration directory found, linking {} -> {}", linkedAwsConfig, realAwsConfig)
            Files.createSymbolicLink(linkedAwsConfig, realAwsConfig)
        }

        return RuntimeEnvironment(tempHome, variables)
    }

    fun getCluster(clusterName: String): JsonObject {
        val result = kops(listOf("get", "clusters", "--name=$clusterName", "--state=s3://${ctx.stateStorageBucket}", "--output=json"))
        if (result.exitValue == 0) {
            return JsonObject(result.outputUTF8())
        } else {
            throw IllegalStateException("Unable to get cluster info!\n\n${result.outputUTF8()}")
        }
    }

    fun getKubernetesConfig(clusterName: String): String {
        val result = kops(listOf("export", "kubecfg", "--name=$clusterName", "--state=s3://${ctx.stateStorageBucket}"))
        if (result.exitValue == 0) {
            return Files.newBufferedReader(environment.path.resolve(".kube/config")).readText()
        } else {
            throw IllegalStateException("Unable to get cluster info!\n\n${result.outputUTF8()}")
        }
    }

    fun version(): String {
        val result = kops(listOf("version"))
        when(result.exitValue) {
            0    -> return result.outputUTF8().split(" ")[1]
            else -> throw RuntimeException("""Process execution failed (exit code: ${result.exitValue})
${result.outputUTF8()}
""")
        }
    }

    private fun kops(args: List<String>,
                     workingDirectory: File? = null): ProcessResult {

        val pe = ProcessExecutor().apply {
            command(listOf(tool.tool) + args)
            redirectOutput(Slf4jStream.ofCaller().asInfo())
            directory(this@KopsTool.environment.path.toFile())
            environment(this@KopsTool.environment.environment)
            readOutput(true)
        }

        logger.debug("""Preparing to run tool '{}'
    Full Command      = {},
    Working Directory = {},
    Environment       = SEE BELOW
        {}""", tool.tool, pe.command, pe.directory, formatEnvironmentVariables(pe.environment))

        return pe.execute()
    }

    private fun formatEnvironmentVariables(vars: Map<String, String>): String {
        val res = StringBuilder()
        for ((k, v) in vars) {
            res.append("\t\t").append("$k = $v")
        }

        return res.toString()
    }
}

fun main(args: Array<String>) {
    val props = Properties()
    props.load(FileInputStream("config/server.properties"))
    for ((name, value) in props) {
        System.setProperty(name.toString(), value.toString())
    }

    val kops = KopsTool(ExternalTool("kops"), KopsContext("loom-state-914373874199"))

    kops.use {
        println(it.version())
        println(it.getCluster("default.k736.net"))
        println(it.getKubernetesConfig("default.k736.net"))
    }
}