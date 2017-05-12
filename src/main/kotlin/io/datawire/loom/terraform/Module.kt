package io.datawire.loom.terraform

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonView


data class Module(
    @JsonView(InternalView::class)
    val name: String,

    @JsonView(TemplateView::class)
    val source: String,

    @get:JsonAnyGetter
    @get:JsonView(TemplateView::class)
    val properties: Map<String, TerraformValue<*>> = emptyMap()
) {

}
