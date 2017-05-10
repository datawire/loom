package io.datawire.loom.dev.model.terraform

import com.fasterxml.jackson.annotation.JsonProperty


data class TerraformTemplate(
    @JsonProperty("provider")
    val providers: Map<String, TerraformProvider>
)