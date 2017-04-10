package io.datawire.loom.proto.fabric.state.api


/**
 * The action to perform once a state is reached toProcessResult a transition.
 *
 * @author plombardi@datawire.io
 */


interface StateAction<in T : State<*>> {

  /**
   * The action to perform once a state transition has completed and a new state is the current state.
   *
   * @param state the current state
   */
  fun execute(state: T)
}