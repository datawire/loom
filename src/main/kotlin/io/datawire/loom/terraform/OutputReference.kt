package io.datawire.loom.terraform

import com.fasterxml.jackson.annotation.JsonView


data class OutputReference(
    @JsonView(InternalView::class)
    val name: String,

    @JsonView(TemplateView::class)
    val value: TerraformValue<*>
) {

    constructor(name: String, value: String): this(name, TerraformString(value))
    constructor(name: String, value: List<String>): this(name, TerraformList(value))
    constructor(name: String, value: Map<String, String>): this(name, TerraformMap(value))
}
