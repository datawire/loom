package io.datawire.loom.kops.cluster


import java.net.URI


data class ClusterEndpoint(
    val apiVersion: String,
    val address: URI,
    val certificateAuthorityData: String,
    val skipTlsVerification: Boolean = false
)
