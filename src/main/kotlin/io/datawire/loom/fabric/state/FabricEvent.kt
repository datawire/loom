package io.datawire.loom.fabric.state

import io.datawire.loom.fabric.state.v2.Event

enum class FabricEvent(override val id: String) : Event {
  CREATE_NETWORK(CREATE_NETWORK.name),
  CREATE_NETWORK_COMPLETE(CREATE_NETWORK_COMPLETE.name),
  CREATE_CLUSTER(CREATE_CLUSTER.name),
  CREATE_CLUSTER_COMPLETE(CREATE_CLUSTER_COMPLETE.name)
}