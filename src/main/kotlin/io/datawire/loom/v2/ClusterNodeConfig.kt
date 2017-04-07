package io.datawire.loom.v2


class ClusterNodeConfig(
    val name      : String,
    val nodeCount : Int = 1,
    val nodeType  : String = "t2.nano"
)