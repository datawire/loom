package io.datawire.loom.proto.fabric.state

import io.datawire.loom.proto.fabric.state.v2.BaseState
import io.datawire.loom.proto.fabric.state.v2.Transition


sealed class FabricState(
    name: String, transitions: Map<FabricEvent, Transition<FabricEvent>>) : BaseState<FabricEvent>(name, transitions)

class FabricCreated(transitions: Map<FabricEvent, Transition<FabricEvent>>)
  : FabricState("fabric:created", transitions)

// ---------------------------------------------------------------------------------------------------------------------
// Cluster States
// ---------------------------------------------------------------------------------------------------------------------

class ClusterCreating(transitions: Map<FabricEvent, Transition<FabricEvent>>)
  : FabricState("fabric.cluster:creating", transitions)

class ClusterDeleting(transitions: Map<FabricEvent, Transition<FabricEvent>>)
  : FabricState("fabric.cluster:deleting", transitions)

class ClusterExists(transitions: Map<FabricEvent, Transition<FabricEvent>>)
  : FabricState("fabric.cluster:exists", transitions)

class ClusterNotExists(transitions: Map<FabricEvent, Transition<FabricEvent>>)
  : FabricState("fabric.cluster:not-exists", transitions)