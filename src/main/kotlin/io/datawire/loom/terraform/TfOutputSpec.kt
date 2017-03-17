package io.datawire.loom.terraform

import com.fasterxml.jackson.annotation.JsonProperty


data class TfOutputSpec(
        @JsonProperty val name: String,
        @JsonProperty val target: String)