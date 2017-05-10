package io.datawire.loom.dev.core

import io.datawire.loom.dev.model.Fabric


interface FabricDao {
  fun createFabric(fabric: Fabric)
  fun deleteFabric(id: String)
  fun fetchFabric(id: String): Fabric?
  fun exists(id: String): Boolean
  fun notExists(id: String) = !exists(id)
}
