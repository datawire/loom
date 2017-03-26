package io.datawire.loom.cluster


data class ClusterContext(
        val name: String,
        val endpoint: ClusterEndpoint,
        val user: ClusterUser) {

    val apiHost: String = endpoint.address.host
    val apiPort: Int    = endpoint.address.port
}
