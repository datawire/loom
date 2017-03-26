package io.datawire.loom.fabric.terraform

import com.fasterxml.jackson.annotation.JsonProperty


data class TfOutputSpec(
        @JsonProperty
        val name: String,

        @JsonProperty
        val target: String)