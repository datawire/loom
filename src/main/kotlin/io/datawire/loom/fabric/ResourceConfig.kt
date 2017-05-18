package io.datawire.loom.fabric

import io.datawire.loom.terraform.TerraformValue


data class ResourceConfig(
    val model      : String,
    val name       : String,
    val parameters : Map<String, TerraformValue<*>>
)
