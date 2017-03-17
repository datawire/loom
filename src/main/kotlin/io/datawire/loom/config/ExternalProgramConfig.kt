package io.datawire.loom.config

import java.nio.file.Path


interface ExternalProgramConfig {
    val executable: Path
}