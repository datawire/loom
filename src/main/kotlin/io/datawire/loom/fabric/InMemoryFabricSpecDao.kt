package io.datawire.loom.fabric

import io.datawire.loom.core.LoomException
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


class InMemoryFabricSpecDao(
    private val map: ConcurrentMap<String, FabricSpec> = ConcurrentHashMap()
) : FabricSpecDao {

  override fun createSpec(spec: FabricSpec): FabricSpec {
    val specWithCreationTime = spec.copy(creationTime = Instant.now())
    val previous = map.putIfAbsent(specWithCreationTime.name, spec)
    return previous?.let { throw LoomException(409) } ?: specWithCreationTime
  }

  override fun updateSpec(model: FabricSpec): FabricSpec {
    return map.put(model.name, model) ?: model
  }

  override fun deleteSpec(id: String) {
    map.remove(id)
  }

  override fun fetchSpec(name: String): FabricSpec? = map[name]

  override fun exists(id: String): Boolean = id in map
}