package io.datawire.loom.fabric

import io.datawire.loom.core.Workspace
import io.datawire.loom.core.aws.AwsCloud

interface FabricService {

  val amazon: AwsCloud

  fun createModel(model: FabricModel): FabricModel
  fun fetchModel(name: String): FabricModel?
  fun fetchFabric(name: String): FabricSpec?
  fun createFabric(config: FabricConfig): FabricSpec
  fun decommissionModel(name: String): FabricModel?
  fun createOrGetWorkspace(name: String): Workspace
  fun registerResourceModel(model: ResourceModel): ResourceModel

  fun fetchKubernetesContext(name: String): String?

  fun addResourceToFabric(name: String, config: ResourceConfig)
  fun removeResourceFromFabric(name: String, resourceName: String)
}
