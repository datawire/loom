package io.datawire.loom.v1.api


inline fun <reified T: Any> jsonType() = "application/vnd.loom.${T::class.simpleName}-v1+json"