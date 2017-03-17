package io.datawire.loom.config


data class S3StateStore(
        val bucket: String,
        val region: String = "us-east-1")
