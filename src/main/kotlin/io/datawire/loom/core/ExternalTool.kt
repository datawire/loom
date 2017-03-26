package io.datawire.loom.core

import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


/**
 * An external tool used by the Loom, for example, Terraform or Kops.
 *
 * @property executableName the executableName of the tool.
 * @property customSearchPaths a list of searchPaths to be searched until the tool is found.
 */
data class ExternalTool(private val executableName    : String,
                        private val customSearchPaths : List<Path> = emptyList()) {

    private val logger = LoggerFactory.getLogger(ExternalTool::class.java)

    private val defaultSearchPaths = listOf(
            "${System.getProperty("user.dir")}/.loom/bin",
            "/bin",
            "/usr/bin",
            "/usr/local/bin",
            "/usr/local/$executableName/bin",
            "${System.getProperty("user.home")}/bin").map { Paths.get(it) }

    val executable: String = resolveExecutable()

    private fun resolveExecutable(): String {
        for (p in (customSearchPaths + defaultSearchPaths)) {
            val candidate = p.resolve(executableName)
            if (Files.isExecutable(candidate)) {
                logger.info("External tool '{}' found at '{}'", executableName, candidate)
                return candidate.toAbsolutePath().toString()
            }

            logger.debug("External tool '{}' not found or not executable at '{}'", executableName, candidate)
        }

        throw IllegalStateException("External tool '$executableName' not found or not executableName on any of the search paths.")
    }
}