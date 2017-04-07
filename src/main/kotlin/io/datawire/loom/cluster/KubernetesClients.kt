package io.datawire.loom.cluster

import io.fabric8.kubernetes.client.ConfigBuilder
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient


object KubernetesClients {

    fun newClient(ctx: ClusterContext, connectTimeout: Int? = null): KubernetesClient {
         val config = ConfigBuilder().apply {
             withApiVersion(ctx.endpoint.apiVersion)
             withMasterUrl(ctx.endpoint.address.toString())
             withTrustCerts(ctx.endpoint.skipTlsVerification)
             withCaCertData(ctx.endpoint.certificateAuthorityData)
             withUsername(ctx.user.username)
             withPassword(ctx.user.password)
             withClientCertData(ctx.user.clientCertificateData)
             withClientKeyData(ctx.user.clientKeyData)

             connectTimeout?.let {
                 withConnectionTimeout(it)
             }
        }.build().apply {
            // If you read the builder status 'username' and 'password' are not used so they're not set regardless of what
            // is configured in the builder. We need them so we set them explicitly.
            username = ctx.user.username
            password = ctx.user.password
        }

        return DefaultKubernetesClient(config)
    }
}