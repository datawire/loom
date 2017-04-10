package io.datawire.loom.v1


class ClusterNodeConfig(
    val name      : String,
    val nodeCount : Int = 1,
    val nodeType  : String = "t2.nano"
)