package io.datawire.loom.cluster

import java.util.*


data class ClusterUser(
        val username: String?,
        val password: String?,
        val clientCertificateData: String?,
        val clientKeyData: String?) {

    val basicAuthCredential: String = Base64.getEncoder().encodeToString("$username:$password".toByteArray())
}