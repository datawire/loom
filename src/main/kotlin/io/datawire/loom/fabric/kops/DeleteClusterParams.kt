package io.datawire.loom.fabric.kops


data class DeleteClusterParams(val clusterName: String) {

    fun toCommandOptions(): List<String> {
        val res = mutableListOf("--name=$clusterName")
        return res
    }
}
