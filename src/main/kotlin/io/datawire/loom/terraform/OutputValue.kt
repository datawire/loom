package io.datawire.loom.terraform


data class OutputValue(
    val type: String,
    val sensitive: Boolean,
    val value: TerraformValue<*>
)
