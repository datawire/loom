package io.datawire.loom.terraform

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonView


data class Backend(
    @JsonView(InternalView::class)
    val name: String,

    @get:JsonAnyGetter
    @JsonView(TemplateView::class)
    val properties: Map<String, String>
)

fun createS3Backend(region: String, bucket: String, key: String, encrypt: Boolean = true) = Backend(
    name = "s3",
    properties = mapOf(
        "encrypt" to encrypt.toString(),
        "region"  to region,
        "bucket"  to bucket,
        "key"     to key
    )
)
