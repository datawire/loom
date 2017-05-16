package io.datawire.loom.fabric


interface FabricModelDao {
  fun createModel(model: FabricModel)
  fun deleteModel(id: String)
  fun updateModel(model: FabricModel): FabricModel
  fun fetchModel(name: String): FabricModel?
  fun exists(id: String): Boolean
  fun notExists(id: String) = !exists(id)
}