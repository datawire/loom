package io.datawire.loom.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant


data class FabricModel(
        @JsonProperty("name")
        val name: String = "default",

        @JsonProperty("allowedRegions")
        val allowedRegions: List<String> = listOf("us-east-1"),

        @JsonProperty("defaultRegion")
        private val defaultRegion : String? = null,

        @JsonProperty("version")
        val version: Int = 1,

        @JsonProperty("creationTime")
        val creationTime: Instant? = null,

        @JsonProperty("domain")
        val domain: String,

        @JsonProperty("networking")
        val networking: FabricNetworking = FabricNetworking("github.com/datawire/loom//src/terraform/network-v2"),

        @JsonProperty("masterType")
        val masterType: String = "t2.nano",

        @JsonProperty("nodeGroups")
        val nodeGroups: List<NodeGroup>  = listOf(NodeGroup("main", 1, "t2.nano"))) {

    val id = "$name-v$version".toLowerCase().replace(Regex("[^a-zA-Z0-9]+"), "-")

    fun resolveDefaultRegion() = defaultRegion?.let { it } ?: allowedRegions.toList().first()
}
