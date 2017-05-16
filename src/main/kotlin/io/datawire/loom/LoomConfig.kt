package io.datawire.loom

import io.datawire.loom.core.aws.AwsConfig


data class LoomConfig(
    val host: String = "0.0.0.0",
    val port: Int = 7000,
    val amazon: AwsConfig = AwsConfig(null, null, null)
)