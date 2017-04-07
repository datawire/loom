package io.datawire.loom.fabric.state

import io.datawire.loom.fabric.state.v2.BaseState
import io.datawire.loom.fabric.state.v2.StateMachine
import io.datawire.loom.fabric.state.v2.Transition


class CreatedNetwork(
    val fsm: StateMachine<FabricEvent>,
    transitions: Map<FabricEvent, Transition<FabricEvent>>
) : BaseState<FabricEvent>(FabricStateName.CREATE_NETWORK.name, transitions) {

  constructor(fsm: StateMachine<FabricEvent>,
              vararg transitions: Pair<FabricEvent, Transition<FabricEvent>>) : this(fsm, transitions.toMap())

  override fun onEnter() {
    try {

      fsm.process(FabricEvent.CREATE_CLUSTER)
    } catch (any: Throwable) {
    }
  }
}