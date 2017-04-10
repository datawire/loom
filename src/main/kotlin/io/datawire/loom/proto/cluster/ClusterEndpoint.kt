package io.datawire.loom.proto.cluster


import java.net.URI


data class ClusterEndpoint(
    val apiVersion: String,
    val address: URI,
    val certificateAuthorityData: String,
    val skipTlsVerification: Boolean = false
)
