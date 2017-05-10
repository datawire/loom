package io.datawire.loom.dev.model


data class Fabric(
    val model: String,
    val fabricName: String
) {

  val id = "$model/$fabricName"
}