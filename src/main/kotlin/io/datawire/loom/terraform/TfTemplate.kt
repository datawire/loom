package io.datawire.loom.terraform

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty


data class TfTemplate(
        @JsonProperty("terraform") val config: Map<String, TfConfig> = emptyMap(),
        @JsonProperty("provider") val providers: Map<String, TfProvider> = emptyMap(),
        @JsonProperty("module") val modules: Map<String, TfModule> = emptyMap(),
        @JsonProperty("output") val outputs: Map<String, TfOutput> = emptyMap())