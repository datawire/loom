package io.datawire.loom.v1.terraform

import com.fasterxml.jackson.annotation.JsonProperty


data class TfOutputSpec(
        @JsonProperty val name: String,
        @JsonProperty val target: String)