package io.datawire.loom.fabric

import io.datawire.loom.kops.Kops
import io.datawire.loom.kops.createMasterInstanceGroupConfigs
import io.datawire.loom.kops.createWorkerInstanceGroupConfigs
import io.datawire.loom.kops.toClusterConfig
import io.datawire.loom.terraform.*
import java.nio.file.Files


interface FabricTask {

  /**
   * Executes the operation or operations of the task.
   */
  fun execute()
}

class BootstrapFabric(
    private val spec: FabricSpec,
    private val fabricService: FabricService
) : FabricTask {

  override fun execute() {
    val fabricWorkspace    = fabricService.createOrGetWorkspace(spec.name)
    val terraformWorkspace = TerraformWorkspace(Files.createDirectories(fabricWorkspace.path.resolve("terraform")))
    val terraform          = Terraform.newTerraform(fabricWorkspace.path, terraformWorkspace)

    terraformWorkspace.configureBackend(createS3Backend(
        region  = fabricService.amazon.stateStorageBucketRegion,
        bucket  = fabricService.amazon.stateStorageBucketName,
        key     = "${spec.name}-infrastructure.tfstate",
        encrypt = true
    ))

    terraformWorkspace.configureProvider(createAwsProvider(spec.region))
    terraform.init()

    fabricService.addTask(CreateCluster(spec, fabricService))
  }
}

class CreateCluster(
    private val spec: FabricSpec,
    private val fabricService: FabricService
) : FabricTask {

  override fun execute() {
    val fabricWorkspace = fabricService.createOrGetWorkspace(spec.name)

    val cluster = spec.toClusterConfig(fabricService.amazon.stateStorageBucketName)
    val masters = spec.createMasterInstanceGroupConfigs()
    val workers = spec.createWorkerInstanceGroupConfigs()

    val kops = Kops.newKops(
        fabricWorkspace.path,
        fabricService.amazon.stateStorageBucketName,
        fabricWorkspace)

    kops.createCluster(cluster)
    masters.forEach { kops.createInstanceGroup(it) }
    workers.forEach { kops.createInstanceGroup(it) }
    kops.createSshPublicKeySecret(spec.clusterDomain, spec.sshPublicKey)
    kops.updateCluster(spec.clusterDomain)

    val tfModule = Module(
        name   = "cluster",
        source = "./cluster"
    )

    fabricService.addTask(AddModuleToTerraform(tfModule, spec, fabricService))
  }
}

class AddModuleToTerraform(
    private val module: Module,
    private val spec: FabricSpec,
    private val fabricService: FabricService
) : FabricTask {

  override fun execute() {
    val fabricWorkspace    = fabricService.createOrGetWorkspace(spec.name)
    val terraformWorkspace = TerraformWorkspace(Files.createDirectories(fabricWorkspace.path.resolve("terraform")))

    val current = terraformWorkspace.fetchTemplate()
    terraformWorkspace.configureTemplate(current.copy(modules = current.modules + Pair(module.name, module)))

    fabricService.addTask(TerraformPlan(spec, fabricService))
  }
}

class TerraformPlan(
    private val spec: FabricSpec,
    private val fabricService: FabricService
) : FabricTask {

  override fun execute() {
    val fabricWorkspace    = fabricService.createOrGetWorkspace(spec.name)
    val terraformWorkspace = TerraformWorkspace(Files.createDirectories(fabricWorkspace.path.resolve("terraform")))

    val tf = Terraform.newTerraform(fabricWorkspace.path, terraformWorkspace)
    tf.get(true)
    val planResult = tf.plan()

    when(planResult) {
      is Difference -> { println("APPLY!") }
      else ->          { println("DO NOT APPLY!") }
    }
  }
}

