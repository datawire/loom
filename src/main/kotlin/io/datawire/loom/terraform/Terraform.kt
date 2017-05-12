package io.datawire.loom.terraform

import io.datawire.loom.core.ExternalTool
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

  fun apply(planResult: PlanResult): Boolean {
    return false
  }

  fun destroy(planResult: PlanResult): Boolean {
    return false
  }

  fun get() {

  }

  fun output() {

  }

  fun validate(): Boolean {
    val result = execute(terraform("validate", "-no-color"), workspace.path, env)
    return when(result.exitCode) {
      0    -> true
      else -> false
    }
  }

  private fun terraform(args: List<String>) = listOf("terraform") + args

  private fun terraform(vararg args: String) = terraform(args.toList())
}
