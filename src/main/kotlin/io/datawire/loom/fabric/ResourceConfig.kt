package io.datawire.loom.fabric


data class ResourceConfig(
    val model      : String,
    val name       : String,
    val parameters : Map<String, String>
)
