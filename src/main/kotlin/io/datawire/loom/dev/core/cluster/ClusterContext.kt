package io.datawire.loom.dev.core.cluster


data class ClusterContext(val name: String, val endpoint: ClusterEndpoint, val user: ClusterUser)
