package io.datawire.loom.dev.core

import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.ProcessResult
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


abstract class ExternalTool(
    executableName: String,
    customSearchPaths: List<Path> = emptyList()
) {

  private val log = LoggerFactory.getLogger(javaClass)

  private val searchPaths = listOf(
      "/bin",
      "/usr/bin",
      "/usr/local/bin",
      "/usr/local/$executableName/bin",
      "${System.getProperty("user.home")}/bin"
  ).map { Paths.get(it) } + customSearchPaths

  protected val executable = resolveExecutable(executableName)

  protected open fun execute(
      workspace: Path,
      command: List<String>,
      variables: Map<String, String> = emptyMap()
  ): ProcessResult {

    val pe = ProcessExecutor().apply {
      command        (command)
      directory      (workspace.toFile())
      environment    (variables)
      redirectOutput (Slf4jStream.ofCaller().asInfo())
      readOutput     (true)
    }

    return pe.execute()
  }

  private fun resolveExecutable(executableName: String): String {
    for (p in searchPaths) {
      val candidate = p.resolve(executableName)
      if (Files.isExecutable(candidate)) {
        log.info("External tool '{}' found at '{}'", executableName, candidate)
        return candidate.toAbsolutePath().toString()
      }

      log.debug("External tool '{}' not found or not executable at '{}'", executableName, candidate)
    }

    throw IllegalStateException("External tool '$executableName' not found or not executableName on any of the search paths.")
  }
}