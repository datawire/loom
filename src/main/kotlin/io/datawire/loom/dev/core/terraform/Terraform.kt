package io.datawire.loom.dev.core.terraform

import io.datawire.loom.dev.core.ExternalTool
import io.datawire.loom.dev.core.Workspace
import io.datawire.loom.proto.data.fromJson
import io.datawire.loom.proto.fabric.terraform.*
import org.slf4j.LoggerFactory


class Terraform(private val workspace: Workspace) : ExternalTool("terraform") {

  private val logger = LoggerFactory.getLogger(javaClass)

  private val env = mapOf("HOME" to workspace.terraformWorkspace().toString())

  fun init() {
    val cmd    = terraform("init", "-no-color", "-backend=true", "-get")
    val result = execute(workspace.terraformWorkspace(), cmd, env)

    if (result.exitValue != 0) {
      throw RuntimeException("Unexpected Terraform exit code: ${result.exitValue}")
    }
  }

  fun validate(): Boolean {
    val result  = execute(workspace.terraformWorkspace(), terraform("validate", "-no-color"), env)
    return when(result.exitValue) {
      0    -> true
      else -> false
    }
  }

  fun plan(destroy: Boolean = false): TfPlanResult {
    val cmd = terraform("plan", "-no-color", "-input=false", "-detailed-exitcode", "-out=plan.out")

    if (destroy) {
      cmd + "-destroy"
    }

    val result = execute(workspace.terraformWorkspace(), cmd, env)
    val planFile = workspace.path.resolve("terraform").resolve("plan.out")

    return when(result.exitValue) {
      0    -> NoDifferences(planFile, destroy)
      1    -> PlanningError(destroy)
      2    -> Differences(planFile, destroy)
      else -> {
        throw IllegalArgumentException("Unexpected Terraform exit code: ${result.exitValue}")
      }
    }
  }

  fun apply(differences: Differences) {
    val cmd = terraform("apply", "-no-color", "-input=false", "${differences.plan}")
    val result = execute(workspace.terraformWorkspace(), cmd, env)

    if (result.exitValue != 0) {
      throw RuntimeException("Unexpected Terraform exit code: ${result.exitValue}")
    }
  }

  fun output(): TfOutputs {
    val cmd    = terraform("output", "-no-color", "-json")
    val result = execute(workspace.terraformWorkspace(), cmd, env)

    return if (result.exitValue == 0) {
      fromJson<TfOutputs>(result.outputUTF8())
    } else {
      throw RuntimeException("Unexpected Terraform exit code: ${result.exitValue}")
    }
  }

  private fun terraform(vararg args: String) = (arrayOf(executable) + args).toMutableList()
}