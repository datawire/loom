package io.datawire.loom.fabric.terraform

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonIgnore


data class TfBackend(
        @get:JsonIgnore
        val type: String,

        @get:JsonAnyGetter
        val params: Map<String, String>)