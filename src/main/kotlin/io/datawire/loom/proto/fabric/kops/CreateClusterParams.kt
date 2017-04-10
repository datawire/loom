package io.datawire.loom.proto.fabric.kops

import java.nio.file.Path


/**
 * Parameters to invoke `kops create cluster ...` with.
 *
 * @property clusterName fully qualified cluster name (e.g. foo.example.org).
 * @property networkId the cloud provider ID of the network to put the cluster into.
 * @property networkCidr the CIDR block of the network.
 * @property availabilityZones the availability zones to spread kubernetes masters and workers across.
 * @property masterType instance size/type for the Kubernetes master nodes.
 * @property masterCount number of Kubernetes master nodes. (default: 1 per availability zone).
 * @property nodeType instance size/type for the Kubernetes worker nodes.
 * @property labels information labels to attach to underlying compute infrastructure consumed by the Kubernetes cluster.
 */

data class CreateClusterParams(
        val clusterName       : String,
        val channel           : String,
        val networkId         : String,
        val networkCidr       : String,
        val availabilityZones : List<String>,
        val masterType        : String,
        val masterCount       : Int?,
        val nodeType          : String,
        val nodeCount         : Int,
        val sshPublicKey      : Path,
        val labels            : Map<String, String> = emptyMap()) {

    fun toCommandOptions(): List<String> {
        val res = mutableListOf(
                "--associate-public-ip=true",
                "--ssh-public-key=${sshPublicKey.toAbsolutePath()}",
                "--cloud=aws",
                "--channel=$channel",
                "--dns=public",
                "--master-size=$masterType",
                "--name=$clusterName",
                "--network-cidr=$networkCidr",
                "--node-size=$nodeType",
                "--node-count=$nodeCount",
                "--topology=public",
                "--vpc=$networkId",
                "--zones=${availabilityZones.joinToString(",")}"
        )

        if (labels.isNotEmpty()) {
            res += "--cloud-labels=${labels.entries.joinToString(",", prefix = "\"", postfix = "\"")}"
        }

        masterCount?.let {
            res += "--master-count=$it"
        }

        return res
    }
}
