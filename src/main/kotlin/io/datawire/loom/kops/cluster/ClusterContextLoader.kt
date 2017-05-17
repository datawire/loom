package io.datawire.loom.kops.cluster


import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.net.URI
import java.nio.file.Path

class ClusterContextLoader(private val mapper: ObjectMapper) {

  fun load(kubeConfigData: String, clusterName: String): ClusterContext {
    val kubeConfig = mapper.readValue<KubeConfig>(kubeConfigData)
    return toContext(kubeConfig, clusterName)
  }

  fun load(kubeConfigPath: Path, clusterName: String): ClusterContext {
    val kubeConfig = mapper.readValue<KubeConfig>(kubeConfigPath.toFile())
    return toContext(kubeConfig, clusterName)
  }

  private fun toContext(config: KubeConfig, clusterName: String): ClusterContext {
    val cluster = config.clusters.find { it.name == clusterName }!!.cluster
    val user = config.users.find { it.name == clusterName }!!.user

    val addr = URI.create(cluster.server!!)
    val port = when {
      addr.port == -1 && addr.scheme == "http" -> 80
      addr.port == -1 && addr.scheme == "https" -> 443
      else -> addr.port
    }

    return ClusterContext(
        name = clusterName,
        endpoint = ClusterEndpoint(
            address = URI(addr.scheme, addr.userInfo, addr.host, port, addr.path, addr.query, addr.fragment),
            apiVersion = cluster.apiVersion,
            certificateAuthorityData = cluster.certificateAuthority!!,
            skipTlsVerification = cluster.insecureSkipTlsVerify
        ),
        user = ClusterUser(
            username = user.username,
            password = user.password,
            clientCertificateData = user.clientCertificateData,
            clientKeyData = user.clientKeyData)
    )
  }

  // -----------------------------------------------------------------------------------------------------------------
  // Machinery for reading Kubernetes configuration files ("kubeconfig")
  // -----------------------------------------------------------------------------------------------------------------

  @JsonIgnoreProperties(ignoreUnknown = true)
  private data class KubeConfig(
      @JsonProperty("apiVersion") val apiVersion: String = "v1",
      @JsonProperty("current-context") val currentContext: String,
      @JsonProperty("clusters") val clusters: List<Cluster> = emptyList(),
      @JsonProperty("users") val users: List<User> = emptyList())

  @JsonIgnoreProperties(ignoreUnknown = true)
  private data class Cluster @JsonCreator constructor(
      @JsonProperty val name: String,
      @JsonProperty val cluster: ClusterParameters)

  @JsonIgnoreProperties(ignoreUnknown = true)
  private data class User(@JsonProperty val name: String, @JsonProperty val user: UserParameters)

  @JsonIgnoreProperties(ignoreUnknown = true)
  private data class UserParameters(
      @JsonProperty("client-certificate-data") val clientCertificateData: String?,
      @JsonProperty("client-key-data") val clientKeyData: String?,
      @JsonProperty("client-certificate") val clientCertificate: Path?,
      @JsonProperty("client-key") val clientKey: Path?,
      @JsonProperty("username") val username: String?,
      @JsonProperty("password") val password: String?
  )

  @JsonIgnoreProperties(ignoreUnknown = true)
  private data class ClusterParameters(
      @JsonProperty("api-version") val apiVersion: String = "v1",
      @JsonProperty("server") val server: String?,
      @JsonProperty("insecure-skip-tls-verify") val insecureSkipTlsVerify: Boolean = false,
      @JsonProperty("certificate-authority-data") val certificateAuthority: String?
  )
}