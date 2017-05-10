package io.datawire.loom.dev.model


data class FabricConfig(
    val model: String,
    val fabricName: String,
    val networkCidr: String,
    val availabilityZones: List<String>
) {

  val id = "$model/$fabricName".toLowerCase()
}
