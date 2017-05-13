package io.datawire.loom.terraform


data class Output(
    val type      : OutputType,
    val sensitive : Boolean,
    val value     : TerraformValue<*>
)
