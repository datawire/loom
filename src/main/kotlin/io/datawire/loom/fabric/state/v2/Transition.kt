package io.datawire.loom.fabric.state.v2


interface Transition<T: Event> {
  val name: String
  fun enter(lookupTarget: (String) -> State<T>?): State<T>?
}

