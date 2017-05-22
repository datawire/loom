package io.datawire.loom.terraform

import io.datawire.loom.core.ExternalTool
import io.datawire.loom.core.Json
import io.datawire.loom.core.resolveExecutable
import io.datawire.loom.fabric.FabricWorkspace
import java.nio.file.Path
import java.nio.file.Paths


class Terraform(
    executable: Path,
    private val workspace: FabricWorkspace
) : ExternalTool(executable) {

  private val json = Json()
  private val env = workspace.environment

  companion object {

    private val terraformExecutable = resolveExecutable(
        name = "terraform",
        searchPaths = setOf(
            "/bin",
            "/usr/local/bin",
            "/usr/bin",
            "${System.getProperty("user.home")}/bin"
        ).map { Paths.get(it) }.toSet()
    )

    fun newTerraform(workspace: FabricWorkspace) = Terraform(terraformExecutable, workspace)
  }

  fun init() {
    val cmd = terraform("init", "-no-color", "-backend=true", "-get")
    val result = execute(cmd, workspace.terraform, env)

    if (result.exitCode != 0) {
      throw RuntimeException("Unexpected Terraform exit code: ${result.exitCode}")
    }
  }

  fun plan(destroy: Boolean = false, planFilename: String = "plan.out"): PlanResult {
    val cmd = terraform("plan", "-no-color", "-input=false", "-detailed-exitcode", "-out=$planFilename")

    if (destroy) {
      cmd + "-destroy"
    }

    val result = execute(cmd, workspace.terraform, env)
    return fromExitCode(result.exitCode, workspace.terraform.resolve(planFilename))
  }

  fun apply(difference: Difference): Boolean {
    val cmd = terraform("apply", "-no-color", "-input=false", difference.planFile.toString())
    val result = execute(cmd, workspace.terraform, env)
    return result.exitCode == 0
  }

  fun get(update: Boolean): Boolean {
    val cmd = terraform("get", "-update=$update")
    val result = execute(cmd, workspace.terraform, env)
    return result.exitCode == 0
  }

  fun output(): Outputs {
    val cmd = terraform("output", "-no-color", "-json")

    val template = workspace.loadTerraformTemplate()

    val result = execute(cmd, workspace.terraform, env)
    return if (result.exitCode == 0) {
      result.output?.let { json.read<Outputs>(it) } ?: Outputs()
    } else if (result.exitCode == 1 && template?.outputs?.isEmpty() ?: true) {
      Outputs()
    } else {
      throw RuntimeException("Retrieving terraform outputRef failed (workspace: ${workspace.terraform})")
    }
  }

  fun validate(): Boolean {
    val result = execute(terraform("validate", "-no-color"), workspace.terraform, env)
    return when(result.exitCode) {
      0    -> true
      else -> false
    }
  }

  fun version(): String {
    val result = execute(terraform("version"), workspace.terraform, env)
    return result.output
        ?.substringAfter("Terraform v")
        ?.substringBefore('\n')
        ?: throw RuntimeException("Unable to retrieve version information from `$executableFile`.")
  }

  private fun terraform(args: List<String>) = listOf(executableFile.toString()) + args

  private fun terraform(vararg args: String) = terraform(args.toList())
}
