package io.datawire.loom.fabric

import io.datawire.loom.core.LoomException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


class InMemoryResourceModelDao(
    private val map: ConcurrentMap<String, ResourceModel> = ConcurrentHashMap()
)  {

  fun createModel(model: ResourceModel) {
    val previous = map.putIfAbsent(model.name, model)
    if (previous != null) {
      throw LoomException(409)
    }
  }

  fun updateModel(model: ResourceModel): ResourceModel {
    return map.put(model.name, model) ?: model
  }

  fun deleteModel(id: String) {
    map.remove(id)
  }

  fun fetchModel(name: String): ResourceModel? = map[name]

  fun exists(id: String): Boolean = id in map
}