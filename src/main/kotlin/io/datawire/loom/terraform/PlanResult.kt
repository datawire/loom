package io.datawire.loom.terraform

import java.nio.file.Path


sealed class PlanResult

data class NoDifference(val planFile: Path) : PlanResult()

data class Difference(val planFile: Path) : PlanResult()

class PlanError : PlanResult()

fun fromExitCode(code: Int, planFile: Path): PlanResult {
  return when(code) {
    0    -> NoDifference(planFile)
    1    -> PlanError()
    2    -> Difference(planFile)
    else -> throw IllegalArgumentException("Invalid exit code ($code) for `terraform plan -detailed-exitcode ...`")
  }
}
