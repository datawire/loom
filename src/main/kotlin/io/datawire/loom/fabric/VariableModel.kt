package io.datawire.loom.fabric

import io.datawire.loom.terraform.TerraformType
import io.datawire.loom.terraform.TerraformValue


data class VariableModel(
    val type     : TerraformType,
    val required : Boolean,
    val value    : TerraformValue<*>?
)