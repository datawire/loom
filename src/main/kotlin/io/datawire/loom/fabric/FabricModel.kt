package io.datawire.loom.fabric

import java.time.Instant


data class FabricModel(
        val name         : String = "default",
        val version      : Int = 1,
        val creationTime : Instant? = null,
        val domain       : String,
        val networking   : FabricNetworking = FabricNetworking("github.com/datawire/loom//src/terraform/network"),
        val masterType   : String = "t2.nano",
        val nodeGroups   : List<NodeGroup> = listOf(NodeGroup("main", 1, "t2.nano")))

data class FabricNetworking(val module: String)

data class NodeGroup(val name: String, val nodeCount: Int = 1, val nodeType: String = "t2.nano")