package io.datawire.loom.terraform

import io.datawire.loom.config.ExternalProgramConfig
import io.datawire.loom.config.S3StateStore
import java.nio.file.Path


data class TerraformConfig(
        override val executable: Path) : ExternalProgramConfig