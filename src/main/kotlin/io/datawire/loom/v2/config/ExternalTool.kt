package io.datawire.loom.v2.config

import io.vertx.core.logging.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


/**
 * An external tool used by the Loom, for example, Terraform or Kops.
 *
 * @property executable the executable of the tool.
 * @property customSearchPaths a list of searchPaths to be searched until the tool is found.
 */
data class ExternalTool(private val executable: String,
                        private val customSearchPaths: List<Path> = emptyList()) {

    private val logger = LoggerFactory.getLogger(ExternalTool::class.java)

    private val defaultSearchPaths = listOf(
            "/bin",
            "/usr/bin",
            "/usr/local/bin",
            "${System.getProperty("user.home")}/bin"
    ).map { Paths.get(it) }

    val tool: String
    init {
        tool = resolveTool()
    }

    private fun resolveTool(): String {
        for (p in (customSearchPaths + defaultSearchPaths)) {
            val candidate = p.resolve(executable)
            if (Files.isExecutable(candidate)) {
                logger.info("External tool '{}' found at '{}'", executable, candidate)
                return candidate.toAbsolutePath().toString()
            }

            logger.debug("External tool '{}' not found or not executable at '{}'", executable, candidate)
        }

        throw IllegalStateException("External tool '$executable' not found or not executable on any of the search paths.")
    }
}