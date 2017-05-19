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
    val variables: Map<String, TerraformValue<*>> = emptyMap()
) {

    fun isDependent(moduleName: String): Boolean {
        for (value in variables.values) {
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



    fun outputRef(outputName: String) = "module.$name.$outputName"

    fun outputString(name: String) = TerraformString("\${${outputRef(name)}}")

    fun outputList(name: String) = TerraformList(TerraformString("\${${outputRef(name)}}").value)
}
