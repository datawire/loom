package io.datawire.loom.fabric

import io.datawire.loom.terraform.TerraformType


class VariableModel(
    val type     : TerraformType,
    val required : Boolean
)