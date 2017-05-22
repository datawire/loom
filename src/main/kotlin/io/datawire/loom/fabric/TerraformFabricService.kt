package io.datawire.loom.fabric

import io.datawire.loom.core.Workspace
import io.datawire.loom.core.aws.AwsCloud
import io.datawire.loom.core.newWorkspace
import io.datawire.loom.kops.Kops
import java.util.concurrent.Executors


class TerraformAndKopsFabricService(
    override val amazon: AwsCloud,
    private val fabricModels: FabricModelDao,
    private val fabricSpecs: FabricSpecDao,
    private val resourceModels: InMemoryResourceModelDao
): FabricService {

  private val work = Executors.newFixedThreadPool(4)

  // -------------------------------------------------------------------------------------------------------------------
  // Fabric Model Operations
  // -------------------------------------------------------------------------------------------------------------------

  override fun createModel(model: FabricModel): FabricModel {
    fabricModels.createModel(model)
    return model
  }

  override fun fetchModel(name: String) = fabricModels.fetchModel(name)

  override fun decommissionModel(name: String) =
      fetchModel(name)?.let { fabricModels.updateModel(it.copy(active = false)) }

  // -------------------------------------------------------------------------------------------------------------------
  // Resource Model Operations
  // -------------------------------------------------------------------------------------------------------------------

  override fun registerResourceModel(model: ResourceModel): ResourceModel {
    resourceModels.createModel(model)
    return model
  }

  fun fetchResourceModel(name: String) = resourceModels.fetchModel(name)

  // -------------------------------------------------------------------------------------------------------------------
  // Fabric Operations
  // -------------------------------------------------------------------------------------------------------------------

  override fun fetchFabric(name: String) = fabricSpecs.fetchSpec(name)

  override fun createFabric(config: FabricConfig): FabricSpec {
    TODO("DEPRECATED; REMOVE")
//    val spec = fetchModel(config.model)
//        ?.run { assembleFabricSpec(this, config) }
//        ?: throw LoomException(404)
//
//    fabricSpecs.createSpec(spec)
//
//    val bootstrapTask = BootstrapFabric(spec, this)
//    addTask(bootstrapTask)
//
//    return spec
  }

  override fun addResourceToFabric(name: String, config: ResourceConfig) {
    TODO("DEPRECATED; REMOVE")

//    fetchFabric(name)
//        ?.let { fabric ->
//          fetchResourceModel(config.model)?.apply {
//            val spec = assemble(fabric, this, config)
//            val addRsrcTask = AddModuleToTerraform(spec.toTerraformModule(), fabric, this@TerraformAndKopsFabricService)
//            addTask(addRsrcTask)
//          }
//        }
  }

  override fun removeResourceFromFabric(name: String, resourceName: String) {

  }

  override fun createOrGetWorkspace(name: String): Workspace {
    return newWorkspace(name)
  }

  // -------------------------------------------------------------------------------------------------------------------
  // Cluster Operations
  // -------------------------------------------------------------------------------------------------------------------

  override fun fetchKubernetesContext(name: String): String? {
    return fetchFabric(name)?.let {
      val workspace = createOrGetWorkspace(name)

      val kubeSvc = KopsKubernetesClusterService({ Kops.newKops(workspace.path, amazon.stateStorageBucketName, workspace) })
      kubeSvc.getClusterContextByName(it.clusterDomain)
    }
  }
}