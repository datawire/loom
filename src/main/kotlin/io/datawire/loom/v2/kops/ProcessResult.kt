package io.datawire.loom.v2.kops

import java.nio.file.Path


data class ProcessResult(val status    : Int,
                         val workspace : Path,
                         val output    : String? = null)

fun toProcessResult(workspace: Path, result: org.zeroturnaround.exec.ProcessResult) =
    ProcessResult(
      status    = result.exitValue,
      workspace = workspace,
      output    = if (result.hasOutput()) result.outputUTF8() else null
    )
