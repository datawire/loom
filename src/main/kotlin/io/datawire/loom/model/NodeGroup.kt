package io.datawire.loom.model


data class NodeGroup(val name: String, val nodeCount: Int = 1, val nodeType: String = "t2.nano")