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

//    fun isClusterAvailable(): Future<Boolean> {
//        val fut = Future.future<Boolean>()
//
//        val http = vertx.createHttpClient(httpOptions)
//        val request = http.get("/api")
//                .putHeader(HttpHeaders.ACCEPT, "application/json")
//                .putHeader(HttpHeaders.AUTHORIZATION, "Basic ${clusterContext.user.basicAuthCredential}")
//
//        request.handler { fut.complete(it.statusCode() / 100 == 2) }
//        request.exceptionHandler { fut.fail(it) }
//
//        request.end()
//
//        return fut
//    }
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

