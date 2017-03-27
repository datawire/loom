package io.datawire.loom.fabric

import io.datawire.loom.fabric.kops.CreateClusterParams
import io.datawire.loom.fabric.kops.DeleteClusterParams
import io.datawire.loom.fabric.kops.KopsTool
import io.datawire.loom.fabric.kops.KopsToolContext
import io.datawire.loom.fabric.terraform.Differences
import io.datawire.loom.fabric.terraform.TerraformTool
import io.datawire.loom.model.FabricStatus
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


sealed class FabricTask(var ctx: FabricTaskContext, val after: FabricTask? = null) {
    abstract fun process()
}

class CreateCluster(ctx: FabricTaskContext) : FabricTask(ctx) {
    override fun process() {
        val model  = ctx.model

        val key = Files.write(ctx.workspace.resolve("ssh_public_key"), model.sshPublicKey.toByteArray())

        val fabric = ctx.fabric

        val params = CreateClusterParams(
                clusterName       = "${fabric.name}.${model.domain}",
                channel           = "stable",
                networkId         = fabric.networkId!!,
                networkCidr       = fabric.networkCidr!!,
                availabilityZones = fabric.availabilityZones,
                masterType        = model.masterType,
                masterCount       = null,
                nodeType          = model.nodeGroups[0].nodeType,
                nodeCount         = model.nodeGroups[0].nodeCount,
                sshPublicKey      = key,
                labels            = emptyMap()
        )

        val kops = KopsTool(ctx.kops, KopsToolContext(ctx.manager.stateBucket, ctx.workspace))
        val success = kops.createCluster(params)
        if (success) {
            ctx.manager.updateFabric(fabric.copy(clusterName = params.clusterName, status = FabricStatus.COMPLETED))
            after?.let { ctx.manager.putTask(it) }
        } else {
            ctx.manager.updateFabric(fabric.copy(status = FabricStatus.FAILED))
        }
    }
}

class DeleteCluster(ctx: FabricTaskContext) : FabricTask(ctx) {
    override fun process() {
        val fabric = ctx.fabric
        val params = DeleteClusterParams(clusterName = fabric.clusterName!!)
        val kops = KopsTool(ctx.kops, KopsToolContext(ctx.manager.stateBucket, ctx.workspace))
        val success = kops.deleteCluster(params)
        if (success) {
            after?.let { ctx.manager.putTask(it) }
        }
    }
}

class TerraformTask(ctx: FabricTaskContext, after: FabricTask? = null) : FabricTask(ctx, after) {

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

        after?.let {
            it.ctx = it.ctx.copy(fabric = updatedFabric)
            ctx.manager.putTask(it)
        }

        logger.info("Completed terraform task!")
    }
}
