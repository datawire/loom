package io.datawire.loom.core

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.nio.file.Files
import java.nio.file.Path


abstract class ExternalTool(private val executableFile: Path) {

  protected val log: Logger = LoggerFactory.getLogger(javaClass)

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