package io.datawire.loom.v2


interface ClusterService {
  fun create(config: ClusterConfig)

  fun delete(name: String)

  fun exists(name: String): Boolean

  fun isAvailable(name: String): Boolean

  fun getByName(name: String): Cluster?
}