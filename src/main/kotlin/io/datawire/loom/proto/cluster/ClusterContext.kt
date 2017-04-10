package io.datawire.loom.proto.cluster


data class ClusterContext(val name: String, val endpoint: ClusterEndpoint, val user: ClusterUser)
