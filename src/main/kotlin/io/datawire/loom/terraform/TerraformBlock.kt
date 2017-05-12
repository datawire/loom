package io.datawire.loom.terraform

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.datawire.loom.terraform.jackson.BackendDeserializer


data class TerraformBlock(
    @JsonDeserialize(contentUsing = BackendDeserializer::class)
    val backend: Map<String, Backend>
)

fun terraformBlock(backend: Backend) = TerraformBlock(mapOf(backend.name to backend))
