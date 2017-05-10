package io.datawire.loom.dev.model


data class CreateFabricParameters(
    val model       : String,
    val fabricName  : String,
    val networkCidr : String = "89.0.0.0/16"
) {

  val fabricId = "$model/$fabricName".toLowerCase()
}