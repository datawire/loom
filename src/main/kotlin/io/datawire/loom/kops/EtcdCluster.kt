package io.datawire.loom.kops


data class EtcdCluster(
    val name: String,
    val etcdMembers: List<String>
)
