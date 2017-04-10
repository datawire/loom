package io.datawire.loom.proto.fabric.state.v2


interface Transition<T: Event> {
  val name: String
  fun enter(lookupTarget: (String) -> State<T>?): State<T>?
}

