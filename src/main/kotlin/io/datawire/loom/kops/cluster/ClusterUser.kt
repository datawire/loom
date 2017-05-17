package io.datawire.loom.kops.cluster


data class ClusterUser(
    val username: String?,
    val password: String?,
    val clientCertificateData: String?,
    val clientKeyData: String?
)
