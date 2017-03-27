package io.datawire.loom.fabric.terraform

import com.fasterxml.jackson.annotation.JsonProperty
import io.datawire.loom.data.toJson
import java.nio.file.Path


data class TfTemplate(
        // TODO: The weird structure of the "terraform" (config) block is a bug. It should be Map<String, TfConfig>
        // Issue: https://github.com/hashicorp/terraform/issues/12919
        @JsonProperty("terraform") val config: List<Map<String, List<Map<String, TfBackend>>>> = emptyList(),
        @JsonProperty("provider")  val providers: Map<String, TfProvider>        = emptyMap(),
        @JsonProperty("module")    val modules: Map<String, TfModule>            = emptyMap(),
        @JsonProperty("output")    val outputs: Map<String, TfOutput>            = emptyMap()) {

    fun write(path: Path) = toJson(path, this)
}

