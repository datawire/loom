package io.datawire.loom.dev.model.terraform


data class TerraformProvider(
    val name: String,
    val properties: Map<String, Any>
)