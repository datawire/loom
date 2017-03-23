package io.datawire.loom.v1.terraform

import io.datawire.loom.v1.config.ExternalProgramConfig
import java.nio.file.Path


data class TerraformConfig(
        override val executable: Path) : ExternalProgramConfig