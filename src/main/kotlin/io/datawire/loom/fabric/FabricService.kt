package io.datawire.loom.fabric

interface FabricService {
  fun createModel(model: FabricModel): FabricModel
  fun fetchModel(name: String): FabricModel?
  fun fetchFabric(name: String): FabricSpec?
  fun createFabric(config: FabricConfig): FabricSpec
  fun decommissionModel(name: String): FabricModel?
}
