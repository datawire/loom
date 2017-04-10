package io.datawire.loom.proto.fabric.state.api


interface TransitionAction {
  fun execute(source: String, target: String, transition: String)
}
