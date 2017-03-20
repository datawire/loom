package io.datawire.loom.terraform

import io.datawire.loom.config.ExternalProgramConfig
import java.nio.file.Path


data class TerraformConfig(
        override val executable: Path) : ExternalProgramConfig