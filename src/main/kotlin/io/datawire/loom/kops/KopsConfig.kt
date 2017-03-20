package io.datawire.loom.kops

import io.datawire.loom.config.ExternalProgramConfig
import java.nio.file.Path


data class KopsConfig(
        override val executable: Path,
        val stateStore: String? = null) : ExternalProgramConfig