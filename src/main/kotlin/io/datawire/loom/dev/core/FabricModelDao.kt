package io.datawire.loom.dev.core

import io.datawire.loom.dev.model.FabricModel


interface FabricModelDao {
  fun createModel(model: FabricModel)
  fun deleteModel(id: String)
  fun fetchModel(id: String): FabricModel?
  fun exists(id: String): Boolean
  fun notExists(id: String) = !exists(id)
}