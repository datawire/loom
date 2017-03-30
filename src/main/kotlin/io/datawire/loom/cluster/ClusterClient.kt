package io.datawire.loom.cluster

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.fabric8.kubernetes.client.KubernetesClient
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import java.nio.file.Paths


class ClusterClient(private val clusterContext: ClusterContext,
                    private val http: OkHttpClient,
                    private val client: KubernetesClient) {

    private val logger = LoggerFactory.getLogger(ClusterClient::class.java)

}

fun main(args: Array<String>) {
    val ctxLoader = ClusterContextLoader(ObjectMapper(YAMLFactory()).registerKotlinModule())
    val ctx       = ctxLoader.load(Paths.get("/home/plombardi/.kube/config"), "default.k736.net")
    val kube      = KubernetesClients.newClient(ctx)

    println(kube.configuration)

    kube.nodes().list().items.forEach {
        println(it)
        println(it.status)
    }
}

