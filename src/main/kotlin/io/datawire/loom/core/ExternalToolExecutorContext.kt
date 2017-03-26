package io.datawire.loom.core

import java.nio.file.Path


data class ExternalToolExecutorContext(
        val workspace: Path,
        val environmentVariables: Map<String, String> = emptyMap())
