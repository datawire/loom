package io.datawire.loom.kops

import io.datawire.loom.config.ExternalProgramConfig
import io.datawire.loom.config.S3StateStore
import java.nio.file.Path


data class KopsConfig(
        override val executable: Path,
        val stateStore: String? = null) : ExternalProgramConfig