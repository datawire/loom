package io.datawire.loom.core

import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.nio.file.Files
import java.nio.file.Path


abstract class ExternalTool(protected val executableFile: Path) {

  init {
    if (!Files.isExecutable(executableFile)) {
      throw IllegalArgumentException("Specified path for execution <$executableFile> is not marked as executable")
    }
  }

  protected fun execute(
      command          : List<String>,
      workingDirectory : Path,
      environment      : Map<String, String> = emptyMap()
  ): ProcessResult {

    val pe = ProcessExecutor().apply {
      command        (command)
      directory      (workingDirectory.toFile())
      environment    (environment)
      redirectOutput (Slf4jStream.ofCaller().asDebug())
      readOutput     (true)
    }

    val res = pe.execute()
    return ProcessResult(res.exitValue, if (res.hasOutput()) res.outputUTF8() else null)
  }
}

fun resolveExecutable(name: String, searchPaths: Set<Path>) =
    searchPaths
        .map  { it.resolve(name) }
        .find { Files.isExecutable(it) }
        ?: throw IllegalStateException("External tool '$name' not found or not on any of the search paths.")