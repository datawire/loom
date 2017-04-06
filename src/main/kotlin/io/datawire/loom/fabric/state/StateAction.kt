package io.datawire.loom.fabric.state

interface StateAction {
  fun execute(state: State<*>)
}