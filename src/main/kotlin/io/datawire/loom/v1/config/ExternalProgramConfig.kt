package io.datawire.loom.v1.config

import java.nio.file.Path


interface ExternalProgramConfig {
    val executable: Path
}