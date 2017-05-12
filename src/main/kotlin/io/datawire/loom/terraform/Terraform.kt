package io.datawire.loom.terraform

import io.datawire.loom.core.ExternalTool
import io.datawire.loom.core.Json
import java.nio.file.Path


class Terraform(
    executable: Path,
    home: Path,
    private val workspace: TerraformWorkspace
) : ExternalTool(executable) {

  private val env = mapOf("HOME" to home.toString())

  fun init() {
    val cmd = terraform("init", "-no-color", "-backend=true", "-get")
    val result = execute(cmd, workspace.path, env)

    if (result.exitCode != 0) {
      throw RuntimeException("Unexpected Terraform exit code: ${result.exitCode}")
    }
  }

  fun plan(destroy: Boolean = false, planFilename: String = "plan.out"): PlanResult {
    val cmd = terraform("plan", "-no-color", "-input=false", "-detailed-exitcode", "-out=$planFilename")

    if (destroy) {
      cmd + "-destroy"
    }

    val result = execute(cmd, workspace.path, env)
    return fromExitCode(result.exitCode, workspace.resolve(planFilename))
  }

  fun apply(difference: Difference): Boolean {
    val cmd = terraform("apply", "-no-color", "-input=false", difference.planFile.toString())
    val result = execute(cmd, workspace.path, env)
    return result.exitCode == 0
  }

  fun get(update: Boolean): Boolean {
    val cmd = terraform("get", "-update=$update")
    val result = execute(cmd, workspace.path, env)
    return result.exitCode == 0
  }

  fun output(): Outputs {
    val cmd = terraform("output", "-no-color", "-json")

    val template = workspace.fetchTemplate()

    val result = execute(cmd, workspace.path, env)
    return if (result.exitCode == 0) {
      result.output?.let { Json.deserialize<Outputs>(it) } ?: Outputs()
    } else if (result.exitCode == 1 && template?.outputs?.isEmpty() ?: true) {
      Outputs()
    } else {
      throw RuntimeException("Terraform output failed (workspace: ${workspace.path})")
    }
  }

  fun validate(): Boolean {
    val result = execute(terraform("validate", "-no-color"), workspace.path, env)
    return when(result.exitCode) {
      0    -> true
      else -> false
    }
  }

  private fun terraform(args: List<String>) = listOf(executableFile.toString()) + args

  private fun terraform(vararg args: String) = terraform(args.toList())
}