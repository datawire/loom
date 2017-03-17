package io.datawire.loom.kops


data class KopsCreateCluster(val availabilityZones: List<String>,
                             val vpcId: String,
                             val vpcCidr: String,
                             val clusterName: String)