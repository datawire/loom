package io.datawire.loom.v1.kops

import io.datawire.loom.v1.config.ExternalProgramConfig
import java.nio.file.Path


data class KopsConfig(
        override val executable: Path,
        val stateStore: String? = null) : ExternalProgramConfig