package io.datawire.loom.fabric

import io.datawire.loom.core.LoomException
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


class InMemoryFabricModelDao(
    private val map: ConcurrentMap<String, FabricModel> = ConcurrentHashMap()
) : FabricModelDao {

  override fun createModel(model: FabricModel): FabricModel {
    val modelWithCreationTime = model.copy(creationTime = Instant.now())
    val previous = map.putIfAbsent(model.name, modelWithCreationTime)

    return previous
        ?.let { throw LoomException(409) }
        ?: modelWithCreationTime
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