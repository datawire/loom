package io.datawire.loom.fabric.terraform

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty


data class TfModule(
        @get:JsonIgnore
        val name: String,

        @JsonProperty
        val source: String,

        @get:JsonAnyGetter
        val inputs: Map<String, Any>)
