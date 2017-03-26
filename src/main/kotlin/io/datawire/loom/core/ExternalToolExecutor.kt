package io.datawire.loom.core

import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.ProcessResult
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream


class ExternalToolExecutor(
        private val command: List<String>,
        private val ctx: ExternalToolExecutorContext) {

    fun execute(): ProcessResult {
        val pe = ProcessExecutor().apply {
            command(this@ExternalToolExecutor.command)
            directory(ctx.workspace.toFile())
            environment(ctx.environmentVariables)
            redirectOutput(Slf4jStream.ofCaller().asInfo())
            readOutput(true)
        }

        return pe.execute()
    }
}