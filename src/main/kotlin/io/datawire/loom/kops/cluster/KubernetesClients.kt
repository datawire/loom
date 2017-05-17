package io.datawire.loom.kops.cluster

import io.fabric8.kubernetes.client.ConfigBuilder
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient


object KubernetesClients {
  fun newClient(ctx: ClusterContext, connectTimeout: Int? = null): KubernetesClient {
    val config = ConfigBuilder().apply {
      withApiVersion     (ctx.endpoint.apiVersion)
      withMasterUrl      (ctx.endpoint.address.toString())
      withTrustCerts     (ctx.endpoint.skipTlsVerification)
      withCaCertData     (ctx.endpoint.certificateAuthorityData)
      withUsername       (ctx.user.username)
      withPassword       (ctx.user.password)
      withClientCertData (ctx.user.clientCertificateData)
      withClientKeyData  (ctx.user.clientKeyData)

      connectTimeout?.let {
        withConnectionTimeout(it)
      }
    }.build()

    return DefaultKubernetesClient(config)
  }
}