package io.datawire.loom.fabric

import io.datawire.loom.aws.AwsProvider
import io.datawire.loom.aws.symlinkAwsConfig
import io.datawire.loom.core.ExternalTool
import io.datawire.loom.data.AwsS3Dao
import io.datawire.loom.exception.*
import io.datawire.loom.fabric.kops.KopsTool
import io.datawire.loom.fabric.kops.KopsToolContext
import io.datawire.loom.fabric.terraform.*
import io.datawire.loom.model.Fabric
import io.datawire.loom.model.FabricModel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executors


class FabricManager(private val terraform     : ExternalTool,
                    private val kops          : ExternalTool,
                    private val awsProvider   : AwsProvider,
                    private val fabricModels  : AwsS3Dao<FabricModel>,
                    private val fabrics       : AwsS3Dao<Fabric>) {

    private val backgroundTasks = Executors.newFixedThreadPool(5)

    private val tasks = ArrayBlockingQueue<FabricTask>(100)

    val stateBucket = awsProvider.stateStorageBucketName

    init {
        backgroundTasks.execute(FabricWorker(this))
        backgroundTasks.execute(FabricWorker(this))
    }

    fun putTask(task: FabricTask) = tasks.put(task)
    fun getTask(): FabricTask     = tasks.take()

    fun updateFabric(fabric: Fabric) {
        fabrics.put(fabric.name, fabric)
    }

    private fun validate(model: FabricModel, fabric: Fabric) {
        fabrics.get(fabric.name)?.let { throw ExistsAlreadyException(FabricExists(it.name)) }
    }

    fun getClusterConfig(fabricName: String): String {
        val fabric = fabrics.get(fabricName) ?: throw NotFoundException(FabricNotFound(fabricName))
        val model  = fabricModels.get(fabric.model) ?: throw NotFoundException(ModelNotFound(fabric.model))

        val kops = KopsTool(kops, KopsToolContext(awsProvider.stateStorageBucketName, createWorkspace(fabricName)))
        return kops.exportKubernetesConfiguration("${fabric.name}.${model.domain}")
    }

    fun create(fabric: Fabric): Fabric {
        val model = fabricModels.get(fabric.model) ?: throw NotFoundException(ModelNotFound(fabric.model))
        validate(model, fabric)

        val resolvedNameFabric = fabric.copy(clusterName = "${fabric.name}.${model.domain}")
        fabrics.put(fabric.name, resolvedNameFabric)
        val workspace = createWorkspace(fabric.name)

        createNetwork(workspace, model, resolvedNameFabric)
        return fabric
    }

    private fun createNetwork(workspace: Path, model: FabricModel, fabric: Fabric) {
        val tfProvider = TfProvider("aws", mapOf("region" to model.region))
        val tfBackend  = TfBackend("s3", mapOf(
                "bucket"     to awsProvider.stateStorageBucketName,
                "key"        to "fabrics/${fabric.name}.tfstate",
                "encrypt"    to "true",
                "region"     to "us-east-1",
                "acl"        to "private",
                "lock_table" to awsProvider.lockTableName
        ))

        val networking = generateNetworkingModule(model.networking.module, mapOf(
                "cidr_block"       to "10.0.0.0/16",
                "name"             to fabric.name
        ))

        val tfTemplate = generateTemplate(tfBackend, tfProvider, listOf(networking))
        tfTemplate.write(workspace.resolve("terraform.tf.json"))

        val fabCtx = FabricTaskContext(model, fabric, this, workspace, terraform, kops)
        val tfTask = TerraformTask(fabCtx, after = CreateCluster(fabCtx))

        putTask(tfTask)
    }

    private fun generateNetworkingModule(source: String, params: Map<String, Any>): Pair<TfModule, List<TfOutput>> {
        val module  = TfModule("main", source, params)
        val outputs = listOf(
                TfOutput("main_vpc_id", "\${module.main.id}"),
                TfOutput("main_vpc_cidr", "\${module.main.cidr_block}"),
                TfOutput("main_vpc_availability_zones", "\${module.main.availability_zones}")
        )

        return Pair(module, outputs)
    }

    private fun generateTemplate(
            config: TfBackend, provider: TfProvider, modulesAndOutputs: List<Pair<TfModule, List<TfOutput>>>): TfTemplate {

        val template = TfTemplate(
                config    = listOf(mapOf("backend" to listOf(mapOf(config.type to config)))),
                providers = mapOf(provider.name to provider))

        return modulesAndOutputs.fold(template) { template, (module, outputs) ->
            template.copy(
                    modules = template.modules + Pair(module.name, module),
                    outputs = template.outputs + outputs.associateBy { it.name }) }
    }

    fun deleteCluster(fabric: Fabric) {
        val model = fabricModels.get(fabric.model) ?: throw NotFoundException(ModelNotFound(fabric.model))
        val fabCtx = FabricTaskContext(model, fabric, this, createWorkspace(fabric.name), terraform, kops)
        val task = DeleteCluster(fabCtx)
        putTask(task)
    }

    private fun createWorkspace(fabricName: String): Path {
        val res = Files.createDirectories(Paths.get("/tmp", "fabric-$fabricName"))
        symlinkAwsConfig(res.resolve(".aws"))
        return res
    }
}