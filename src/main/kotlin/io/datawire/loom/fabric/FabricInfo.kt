package io.datawire.loom.fabric

import java.time.Instant


data class FabricInfo(
        val name         : String,
        val creationTime : Instant,
        val owner        : String
)

data class FabricNetwork(
        val id                       : String,
        val kubernetesSubnets        : List<String> = emptyList(),
        val externalResourcesSubnets : List<String> = emptyList())

data class ClusterInfo(
        val kubernetesVersion: String
)

data class AdminCredential(val username: String = "admin", val password: String)