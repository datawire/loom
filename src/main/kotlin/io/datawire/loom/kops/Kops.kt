package io.datawire.loom.kops


import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.ProcessResult
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.nio.file.Paths


class Kops(private val config: KopsConfig) {

    private val logger = LoggerFactory.getLogger(javaClass)

    val kops get() = config.executable.toString()
    val version by lazy { readVersion() }

    fun rawClusterInfo(clusterName: String): JsonObject {
        logger.debug("Querying for cluster info (name: {})", clusterName)
        val res = kops("get", "clusters",
                "--full",
                "--name=$clusterName",
                "--state=s3://datawire-loom",
                "--output=json"
        )

        if (res.exitValue == 0) {
            return JsonObject(res.outputUTF8())
        } else {
            throw IllegalStateException("Unable to create cluster info!\n\n${res.outputUTF8()}")
        }
    }

    fun createCluster(create: KopsCreateCluster): JsonObject {
        logger.debug("Creating Cluster {}", create)

        val result = kops("create", "cluster",
                "--zones=${create.availabilityZones.joinToString(",")}",
                "--vpc=${create.vpcId}",
                "--network-cidr=${create.vpcCidr}",
                "--networking=kubenet",
                "--name=${create.clusterName}",
                "--state=${this.config.stateStore}",
                "--yes")

        if (result.exitValue == 0) {
            return JsonObject(result.outputUTF8())
        } else {
            throw IllegalStateException("Unable to create cluster info!\n\n${result.outputUTF8()}")
        }
    }

    fun getClusterInfo(clusterName: String): JsonObject {
        val result = kops("get", "clusters", "--name=$clusterName", "--output=json")
        if (result.exitValue == 0) {
            return JsonObject(result.outputUTF8())
        } else {
            throw IllegalStateException("Unable to get cluster info!\n\n${result.outputUTF8()}")
        }
    }

    private fun kops(vararg args: String): ProcessResult {
        val pe = ProcessExecutor().apply {
            command(listOf(kops) + args)
            redirectOutput(Slf4jStream.ofCaller().asInfo())
            directory(Paths.get("workspace").resolve("").toFile())
            readOutput(true)
        }

        return pe.execute()
    }

    private fun readVersion(): String {
        val pe = ProcessExecutor().apply {
            command(kops, "version")
            redirectOutput(Slf4jStream.ofCaller().asInfo())
            readOutput(true)
        }

        return pe.execute().outputUTF8()
    }
}