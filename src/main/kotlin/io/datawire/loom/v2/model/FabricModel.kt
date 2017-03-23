package io.datawire.loom.v2.model

import java.time.Instant


data class FabricModel(
        val name           : String           = "default",
        val allowedRegions : Set<String>      = setOf("us-east-1"),
        val version        : Int              = 1,
        val creationTime   : Instant?         = null,
        val domain         : String,
        val networking     : FabricNetworking = FabricNetworking("github.com/datawire/loom//src/terraform/network-v2"),
        val masterType     : String           = "t2.nano",
        val nodeGroups     : List<NodeGroup>  = listOf(NodeGroup("main", 1, "t2.nano"))) {

    val id = "$name-v$version".toLowerCase().replace(Regex("[^a-zA-Z0-9]+"), "-")
}

data class NodeGroup(val name: String, val nodeCount: Int = 1, val nodeType: String = "t2.nano")