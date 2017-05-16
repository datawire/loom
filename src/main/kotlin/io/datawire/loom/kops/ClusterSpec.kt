package io.datawire.loom.kops


data class ClusterSpec(
    val api                 : Map<String, Map<String, Any>>,
    val authorization       : Map<String, Map<String, Any>>,
    val channel             : String,
    val cloudProvider       : String,
    val cloudLabels         : Map<String, String>,
    val configBase          : String,
    val dnsZone             : String,
    val etcdClusters        : List<EtcdCluster>,
    val kubernetesVersion   : String,
    val kubernetesApiAccess : List<String>,
    val masterPublicName    : String,
    val networkCIDR         : String,
    val networkID           : String? = null,
    val networking          : Map<String, Map<String, Any>>,
    val nonMasqueradeCIDR   : String,
    val sshAccess           : List<String>,
    val subnets             : List<SubnetSpec>,
    val topology            : TopologySpec
)
