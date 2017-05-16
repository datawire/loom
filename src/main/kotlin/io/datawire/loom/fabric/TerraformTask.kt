package io.datawire.loom.fabric

import io.datawire.loom.terraform.Difference
import io.datawire.loom.terraform.PlanResult
import io.datawire.loom.terraform.Terraform
import java.util.concurrent.Callable


//sealed class TerraformTask<T>(protected val terraform: Terraform) : Callable<T>
//
//class TerraformPlan(
//    terraform: Terraform,
//    private val destroy: Boolean = false
//) : TerraformTask<PlanResult>(terraform) {
//
//  override fun call(): PlanResult = terraform.plan(destroy)
//}
//
//class TerraformApply(
//    terraform: Terraform,
//    private val diff: Difference
//) : TerraformTask<Boolean>(terraform) {
//
//  override fun call() = terraform.apply(diff)
//}
