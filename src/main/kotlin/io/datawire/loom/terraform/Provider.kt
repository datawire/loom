package io.datawire.loom.terraform

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonView


data class Provider(
    @JsonView(InternalView::class)
    val name: String,

    @get:JsonView(TemplateView::class)
    @get:JsonAnyGetter
    val properties : Map<String, TerraformValue<*>>
)

fun createAwsProvider(region: String) = Provider("aws", mapOf("region" to TerraformString(region)))
