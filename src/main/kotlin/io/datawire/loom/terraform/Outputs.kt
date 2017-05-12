package io.datawire.loom.terraform


data class Outputs(private val values: Map<String, TerraformValue<*>> = emptyMap())