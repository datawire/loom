package io.datawire.loom.terraform


data class Outputs(private val values: Map<String, Output> = emptyMap()) {

  val size = values.size

  fun hasOutput(name: String) = name in values

  fun getOutput(name: String) = values[name]
}
