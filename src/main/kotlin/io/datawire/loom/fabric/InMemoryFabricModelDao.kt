package io.datawire.loom.fabric

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


class InMemoryFabricModelDao(
    private val map: ConcurrentMap<String, FabricModel> = ConcurrentHashMap()
) : FabricModelDao {

  override fun createModel(model: FabricModel) {
    val previous = map.putIfAbsent(model.name, model)
    if (previous != null) {
      throw io.datawire.loom.core.LoomException(409)
    }
  }

  override fun updateModel(model: FabricModel): FabricModel {
    return map.put(model.name, model) ?: model
  }

  override fun deleteModel(id: String) {
    map.remove(id)
  }

  override fun fetchModel(name: String): FabricModel? = map[name]

  override fun exists(id: String): Boolean = id in map
}