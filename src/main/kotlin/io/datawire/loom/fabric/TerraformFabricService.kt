package io.datawire.loom.fabric


class TerraformAndKopsFabricService(
    private val fabricModels : FabricModelDao,
    private val fabricSpecs  : FabricSpecDao
): FabricService {

  override fun createModel(model: FabricModel): FabricModel {
    fabricModels.createModel(model)
    return model
  }

  override fun fetchModel(name: String): FabricModel? {
    return fabricModels.fetchModel(name)
  }

  override fun fetchFabric(name: String): FabricSpec? {
    return fabricSpecs.fetchSpec(name)
  }

  override fun createFabric(config: FabricConfig): FabricSpec {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun decommissionModel(name: String): FabricModel? {
    TODO()
  }
}