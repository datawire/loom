package io.datawire.loom.dev.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import io.datawire.loom.dev.core.kubernetes.KubernetesClusterManager
import io.datawire.loom.dev.model.CreateFabricParameters
import io.datawire.loom.dev.model.Fabric
import io.datawire.loom.dev.model.FabricConfig
import io.datawire.loom.dev.model.FabricModel
import io.datawire.loom.proto.exception.fabricExists
import io.datawire.loom.proto.exception.modelNotExists
import spark.Request
import spark.Response
import java.util.concurrent.Executors


class FabricController(
    private val jsonMapper     : ObjectMapper,
    private val validator      : FabricValidator,
    private val fabricModels   : FabricModelDao,
    private val fabrics        : FabricDao,
    private val clusterManager : KubernetesClusterManager
) {

  private val backgroundTasks = Executors.newFixedThreadPool(4)

  fun createFabric(request: Request, response: Response) {
    val parameters = toCreateParameters(request.body())

    if (!fabrics.exists(parameters.fabricId)) {
      val model  = fabricModels.fetchModel(parameters.model) ?: throw modelNotExists(parameters.model)
      val config = createFabricConfig(model, parameters)

      val workspace = createWorkspace(config.fabricName)
      workspace.createDirectory("terraform")



    } else {
      throw fabricExists(parameters.fabricId)
    }
  }

  fun createCluster(request: Request, response: Response) {

  }

  fun deleteFabric(request: Request, response: Response) {
    val id = request.params(":id")
    fabrics.deleteFabric(id)
    response.status(204)
  }

  fun fetchFabric(request: Request, response: Response): Fabric? {
    return fabrics.fetchFabric(request.params(":id"))
  }

  fun fetchCluster(request: Request, response: Response) {

  }

  private fun toCreateParameters(text: String, validate: Boolean = true): CreateFabricParameters {
    val json = jsonMapper.readTree(text)

    if (validate) {
      validator.validate(json)
    }

    return jsonMapper.treeToValue(json)
  }

  private fun createFabricConfig(model: FabricModel, createParams: CreateFabricParameters): FabricConfig {
    return FabricConfig(
        model             = model.name,
        fabricName        = createParams.fabricName.toLowerCase(),
        networkCidr       = createParams.networkCidr,
        availabilityZones = listOf()
    )
  }
}
