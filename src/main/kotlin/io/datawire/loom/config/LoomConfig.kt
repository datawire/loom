package io.datawire.loom.config

import io.datawire.loom.aws.AwsConfig
import io.datawire.loom.core.ExternalTool


data class LoomConfig(
        val host      : String       = "0.0.0.0",
        val port      : Int          = 7000,
        val amazon    : AwsConfig    = AwsConfig(null, null, null),
        val terraform : ExternalTool = ExternalTool("terraform"),
        val kops      : ExternalTool = ExternalTool("kops"))
