package io.datawire.loom.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant


data class FabricModel(
        @JsonProperty("name")
        val name: String = "default",

        @JsonProperty("region")
        val region: String,

        @JsonProperty("version")
        val version: Int = 1,

        @JsonProperty("creationTime")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "UTC")
        val creationTime: Instant? = null,

        @JsonProperty("domain")
        val domain: String,

        @JsonProperty("networking")
        val networking: FabricNetworkingModel = FabricNetworkingModel("github.com/datawire/loom//src/terraform/network-v2"),

        @JsonProperty("masterType")
        val masterType: String = "t2.small",

        @JsonProperty("nodeGroups")
        val nodeGroups: List<NodeGroup>  = listOf(NodeGroup("main", 1, "t2.nano")),

        @JsonProperty("sshPublicKey")
        val sshPublicKey: String) {

    val id = "$name-v$version".toLowerCase().replace(Regex("[^a-zA-Z0-9]+"), "-")
}
