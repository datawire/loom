package io.datawire.loom.fabric.terraform


class TerraformRunner(private val terraform: TerraformTool) : Runnable {

    override fun run() {
        terraform.init()

        val plan = terraform.plan()
        when(plan) {
            is Differences -> terraform.apply(plan)
        }
    }
}