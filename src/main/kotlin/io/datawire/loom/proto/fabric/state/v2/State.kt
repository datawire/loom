package io.datawire.loom.proto.fabric.state.v2


interface State<T: Event> {
  val name: String

  val transitions: Map<T, Transition<T>>

  fun enter()

  fun getTransition(event: T) = transitions[event]

  fun isTerminal() = transitions.isEmpty()

  fun isNotTerminal() = !isTerminal()
}