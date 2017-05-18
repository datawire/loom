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

    fun isDependent(moduleName: String): Boolean {
        for (value in properties.values) {
            if (when(value) {
                is TerraformString -> { value.value.contains("module.$name") }
                is TerraformList   -> { value.value.any { it.contains("module.$name") } }
                is TerraformMap    -> { value.value.values.any { it.contains("module.$name") } }
            }) {
                return true
            }
        }

        return false
    }
}
