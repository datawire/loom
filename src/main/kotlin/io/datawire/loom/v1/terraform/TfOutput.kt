package io.datawire.loom.v1.terraform

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty


data class TfOutput(
        @get:JsonIgnore val name: String,
        @JsonProperty val value: Any?)
