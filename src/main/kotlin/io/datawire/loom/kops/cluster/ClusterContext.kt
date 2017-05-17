package io.datawire.loom.kops.cluster


data class ClusterContext(val name: String, val endpoint: ClusterEndpoint, val user: ClusterUser)
