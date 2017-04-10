package io.datawire.loom.proto.core

import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.ProcessResult
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream


class ExternalToolExecutor(
    private val command: List<String>,
    private val context: ExternalToolExecutorContext
) {

    fun execute(): ProcessResult {
        val pe = ProcessExecutor().apply {
            command(this@ExternalToolExecutor.command)
            directory(context.workspace.toFile())
            environment(context.environmentVariables)
            redirectOutput(Slf4jStream.ofCaller().asInfo())
            readOutput(true)
        }

        return pe.execute()
    }
}