package io.datawire.loom.dev.core.cluster


import java.net.URI


data class ClusterEndpoint(
    val apiVersion: String,
    val address: URI,
    val certificateAuthorityData: String,
    val skipTlsVerification: Boolean = false
)
