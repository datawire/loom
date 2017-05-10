package io.datawire.loom.dev.core

import io.datawire.loom.dev.model.FabricModel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


class InMemoryFabricModelDao(
    private val map: ConcurrentMap<String, FabricModel> = ConcurrentHashMap()
) : FabricModelDao {

  override fun createModel(model: FabricModel) {
    map.putIfAbsent(model.name, model)
  }

  override fun deleteModel(id: String) {
    map.remove(id)
  }

  override fun fetchModel(id: String): FabricModel? = map[id]

  override fun exists(id: String): Boolean = id in map
}
