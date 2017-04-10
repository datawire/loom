package io.datawire.loom.proto.fabric.state

import io.datawire.loom.proto.fabric.state.v2.BaseState
import io.datawire.loom.proto.fabric.state.v2.StateMachine
import io.datawire.loom.proto.fabric.state.v2.Transition


class CreateNetwork(
    val fsm: StateMachine<FabricEvent>,
    transitions: Map<FabricEvent, Transition<FabricEvent>>
) : BaseState<FabricEvent>(FabricStateName.CREATE_NETWORK.name, transitions) {

  constructor(fsm: StateMachine<FabricEvent>,
              vararg transitions: Pair<FabricEvent, Transition<FabricEvent>>) : this(fsm, transitions.toMap())

  override fun onEnter() {
    try {

      fsm.process(FabricEvent.CREATE_NETWORK_COMPLETE)
    } catch (any: Throwable) {
    }
  }
}