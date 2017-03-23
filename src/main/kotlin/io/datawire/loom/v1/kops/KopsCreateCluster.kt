package io.datawire.loom.v1.kops


data class KopsCreateCluster(val availabilityZones: List<String>,
                             val vpcId: String,
                             val vpcCidr: String,
                             val clusterName: String)