package io.datawire.loom.dev.model


data class FabricModel(
    val name: String,
    val region: String,
    val domain: String?,
    val masterType: String,
    val masterCount: Int? = 1,
    val sshPublicKey: String
)
