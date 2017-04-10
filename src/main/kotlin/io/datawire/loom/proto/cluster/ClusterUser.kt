package io.datawire.loom.proto.cluster


data class ClusterUser(
    val username: String?,
    val password: String?,
    val clientCertificateData: String?,
    val clientKeyData: String?
)
