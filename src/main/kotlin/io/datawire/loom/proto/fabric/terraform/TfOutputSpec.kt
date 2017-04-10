package io.datawire.loom.proto.fabric.terraform

import com.fasterxml.jackson.annotation.JsonProperty


data class TfOutputSpec(
        @JsonProperty
        val name: String,

        @JsonProperty
        val target: String)