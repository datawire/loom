package io.datawire.loom.fabric


interface FabricSpecDao {
  fun createSpec(spec: FabricSpec)
  fun deleteSpec(id: String)
  fun updateSpec(model: FabricSpec): FabricSpec
  fun fetchSpec(name: String): FabricSpec?
  fun exists(id: String): Boolean
  fun notExists(id: String) = !exists(id)
}