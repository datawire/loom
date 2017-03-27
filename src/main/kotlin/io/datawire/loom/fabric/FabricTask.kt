package io.datawire.loom.fabric

import io.datawire.loom.fabric.kops.CreateClusterParams
import io.datawire.loom.fabric.terraform.Differences
import io.datawire.loom.fabric.terraform.TerraformTool
import io.datawire.loom.model.FabricStatus
import org.slf4j.LoggerFactory


sealed class FabricTask(val ctx: FabricTaskContext) {
    abstract fun process()
}

class CreateCluster(ctx: FabricTaskContext) : FabricTask(ctx) {
    override fun process() {
        val model  = ctx.model
        val fabric = ctx.fabric

        val params = CreateClusterParams(
                clusterName       = "${fabric.name}.${model.domain}",
                channel           = "stable",
                networkId         = "",
                networkCidr       = "",
                availabilityZones = emptyList(),
                masterType        = model.masterType,
                masterCount       = null,
                nodeType          = model.nodeGroups[0].nodeType,
                nodeCount         = model.nodeGroups[0].nodeCount,
                sshKeyName        = null,
                labels            = emptyMap()
        )

        val kops = ctx.kops
    }
}

class DeleteCluster(ctx: FabricTaskContext) : FabricTask(ctx) {
    override fun process() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class TerraformTask(ctx: FabricTaskContext) : FabricTask(ctx) {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun process() {
        logger.info("Started terraform task!")

        val terraform = TerraformTool(ctx.terraform, ctx.workspace)
        terraform.init()

        val plan = terraform.plan()
        when(plan) {
            is Differences -> terraform.apply(plan)
        }

        val outputs = terraform.output()
        val updatedFabric = ctx.fabric.copy(
                networkId         = outputs.getOutputAsString("main_vpc_id"),
                networkCidr       = outputs.getOutputAsString("main_vpc_cidr"),
                availabilityZones = outputs.getOutputAsList("main_vpc_availability_zones"),
                status            = FabricStatus.TERRAFORM_RUNNING
        )

        ctx.manager.updateFabric(updatedFabric)

        logger.info("Completed terraform task!")
    }
}
