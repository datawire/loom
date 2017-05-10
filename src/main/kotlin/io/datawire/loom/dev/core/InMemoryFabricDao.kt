package io.datawire.loom.dev.core

import io.datawire.loom.dev.model.Fabric
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


class InMemoryFabricDao(
    private val map: ConcurrentMap<String, Fabric> = ConcurrentHashMap()
) : FabricDao {

  override fun createFabric(fabric: Fabric) {
    map.putIfAbsent(fabric.id, fabric)
  }

  override fun deleteFabric(id: String) {
    map.remove(id)
  }

  override fun fetchFabric(id: String): Fabric? = map[id]

  override fun exists(id: String): Boolean = id in map
}
