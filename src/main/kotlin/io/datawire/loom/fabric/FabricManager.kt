package io.datawire.loom.fabric

import io.datawire.loom.aws.AwsProvider
import io.datawire.loom.core.ExternalTool
import io.datawire.loom.data.AwsS3Dao
import io.datawire.loom.exception.ExistsAlreadyException
import io.datawire.loom.exception.FabricExists
import io.datawire.loom.exception.ModelNotFound
import io.datawire.loom.exception.NotFoundException
import io.datawire.loom.fabric.terraform.*
import io.datawire.loom.model.Fabric
import io.datawire.loom.model.FabricModel
import io.datawire.loom.model.FabricStatus
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Executors


class FabricManager(private val terraform     : ExternalTool,
                    private val kops          : ExternalTool,
                    private val awsProvider   : AwsProvider,
                    private val fabricModels  : AwsS3Dao<FabricModel>,
                    private val fabrics       : AwsS3Dao<Fabric>) {

    private val backgroundTasks = Executors.newFixedThreadPool(5)

    private fun validate(model: FabricModel, fabric: Fabric) {
        fabrics.get(fabric.name)?.let { throw ExistsAlreadyException(FabricExists(it.name)) }
    }

    fun create(fabric: Fabric): Fabric {
        val model = fabricModels.get(fabric.model) ?: throw NotFoundException(ModelNotFound(fabric.model))
        validate(model, fabric)

        val workspace = createWorkspace(fabric.name)
        val updatedFabric = fabric.copy(status = FabricStatus.PROVISION_NETWORK_STARTED)
        createNetwork(workspace, model, updatedFabric)

        return updatedFabric
    }

    private fun createNetwork(workspace: Path, model: FabricModel, fabric: Fabric) {
        val tfProvider = TfProvider("aws", mapOf("region" to model.resolveDefaultRegion()))
        val tfConfig   = TfConfig("s3", mapOf(
                "bucket"     to awsProvider.stateStorageBucket,
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

        val tfTemplate = generateTemplate(tfConfig, tfProvider, listOf(networking))
        tfTemplate.write(workspace.resolve("terraform.tf.json"))

        val terraform = TerraformTool(terraform, workspace)
        backgroundTasks.submit(TerraformRunner(terraform))
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
            config: TfConfig, provider: TfProvider, modulesAndOutputs: List<Pair<TfModule, List<TfOutput>>>): TfTemplate {

        val template = TfTemplate(
                config    = listOf(mapOf(config.type to listOf(config))),
                providers = mapOf(provider.name to provider))

        return modulesAndOutputs.fold(template) { template, (module, outputs) ->
            template.copy(
                    modules = template.modules + Pair(module.name, module),
                    outputs = template.outputs + outputs.associateBy { it.name }) }
    }

    fun createCluster(fabric: Fabric) {

    }

    fun deleteNetwork(fabric: Fabric) {

    }

    fun deleteCluster(fabric: Fabric) {

    }

    private fun createWorkspace(fabricName: String) = Files.createDirectories(Paths.get("/tmp", "fabric-$fabricName"))
}