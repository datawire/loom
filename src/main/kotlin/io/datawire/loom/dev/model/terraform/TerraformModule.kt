package io.datawire.loom.dev.model.terraform

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonView


data class TerraformModule(
    val name: String,

    @JsonView(TerraformView::class)
    val source: String,

    @get:JsonAnyGetter
    @get:JsonView(TerraformView::class)
    val variables: Map<String, TfValue<*>>
)