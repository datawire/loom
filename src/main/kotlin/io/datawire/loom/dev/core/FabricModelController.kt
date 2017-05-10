package io.datawire.loom.dev.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import io.datawire.loom.dev.model.FabricModel
import io.datawire.loom.proto.exception.modelExists
import spark.Request
import spark.Response

class FabricModelController(
    private val jsonMapper: ObjectMapper,
    private val validator: FabricModelValidator,
    private val fabricModels: FabricModelDao
) {

  fun createModel(request: Request, response: Response) {
    val fabricModel = jsonToFabricModel(request.body())

    if (!fabricModels.exists(fabricModel.name)) {
      fabricModels.createModel(fabricModel)
    } else {
      throw modelExists(fabricModel.name)
    }
  }

  fun deleteModel(request: Request, response: Response) {
    val id = request.params(":id")
    fabricModels.deleteModel(id)
    response.status(204)
  }

  fun fetchModel(request: Request, response: Response): FabricModel? {
    return fabricModels.fetchModel(request.params(":id"))
  }

  private fun jsonToFabricModel(text: String, validate: Boolean = true): FabricModel {
    val json = jsonMapper.readTree(text)

    if (validate) {
      validator.validate(json)
    }

    return jsonMapper.treeToValue(json)
  }
}
