package io.datawire.loom.fabric.state


interface TransitionAction {
  fun execute(source: State<*>, target: State<*>, transition: String)
}
