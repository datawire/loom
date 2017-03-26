package io.datawire.loom.fabric.kops


/**
 * Parameters to invoke `kops create cluster ...` with.
 *
 * @property clusterName fully qualified cluster name (e.g. foo.example.org).
 * @property networkId the cloud provider ID of the network to put the cluster into.
 * @property networkCidr the CIDR block of the network.
 * @property availabilityZones the availability zones to spread kubernetes masters and workers across.
 * @property masterType instance size/type for the Kubernetes master nodes.
 * @property masterCount number of Kubernetes master nodes.
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
        val masterCount       : Int? = null,
        val nodeType          : String,
        val nodeCount         : Int,
        val sshKeyName        : String?,
        val labels            : Map<String, String> = emptyMap()) {

    fun toCommandOptions(): List<String> {
        val res = mutableListOf(
                "--associate-public-ip=true",
                "--cloud=aws",
                "--cloud-labels=${labels.entries.joinToString(",", prefix = "\"", postfix = "\"")}",
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

        masterCount?.let { res += "--master-count=$it" }
        sshKeyName?.let  { res += "--ssh-public-key=$it" }

        return res
    }
}
