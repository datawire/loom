package io.datawire.loom.fabric.terraform

import io.datawire.loom.core.ExternalTool
import io.datawire.loom.core.ExternalToolExecutor
import io.datawire.loom.core.ExternalToolExecutorContext
import org.slf4j.LoggerFactory
import java.nio.file.Path


class TerraformTool(
        private val tool: ExternalTool,
        private val workspace: Path) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun init() {
        val execCtx = prepareExecutionContext()
        val execCmd = terraform("init", "-no-color", "-backend=true", "-get")
        val result  = ExternalToolExecutor(execCmd, execCtx).execute()

        if (result.exitValue != 0) {
            throw RuntimeException("""Unexpected Terraform exit code: ${result.exitValue}
    Context => $execCtx
    Command => '$execCmd'""")
        }
    }

    fun validate(): Boolean {
        val execCtx = prepareExecutionContext()
        val result  = ExternalToolExecutor(terraform("validate", "-no-color"), execCtx).execute()
        return when(result.exitValue) {
            0    -> true
            else -> false
        }
    }

    fun plan(destroy: Boolean = false): TfPlanResult {
        val execCtx = prepareExecutionContext()
        val execCmd = terraform("plan", "-no-color", "-input=false", "-out=plan.out")

        if (destroy) {
            execCmd + "-destroy"
        }

        val result   = ExternalToolExecutor(execCmd, execCtx).execute()
        val planFile = execCtx.workspace.resolve("plan.out")
        return when(result.exitValue) {
            0    -> NoDifferences(planFile, destroy)
            1    -> PlanningError(destroy)
            2    -> Differences(planFile, destroy)
            else -> {
                throw IllegalArgumentException("""Unexpected Terraform exit code: ${result.exitValue}
    Context => $execCtx
    Command => '$execCmd'""")
            }
        }
    }

    fun apply(differences: Differences) {
        val execCtx = prepareExecutionContext()
        val execCmd = terraform("apply", "-no-color", "-input=false", "${differences.plan}")

        val result = ExternalToolExecutor(execCmd, execCtx).execute()

        if (result.exitValue != 0) {
            throw RuntimeException("""Unexpected Terraform exit code: ${result.exitValue}
    Context => $execCtx
    Command => '$execCmd'""")
        }
    }

    private fun terraform(vararg args: String) = (arrayOf(tool.executable) + args).toMutableList()

    private fun prepareExecutionContext(): ExternalToolExecutorContext {
        return ExternalToolExecutorContext(workspace)
    }
}
