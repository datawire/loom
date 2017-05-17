package io.datawire.loom.fabric


data class ResourceModel(
    val name      : String,
    val source    : String,
    val variables : Map<String, VariableModel>
)
