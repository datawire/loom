package io.datawire.loom.fabric.state


interface State<T> {
  val name: String
  fun enter()
  fun getTransition(event: T): Transition<T>?
}

/**
 * Implements the [State] interface and can be used easily as a delegate for custom state implementations.
 */
data class BasicState<T: Any>(override val name             : String,
                              private  val transitions      : List<Transition<T>>,
                              private  val onTransitionInto : List<StateAction>) : State<T> {

  override fun enter() = onTransitionInto.forEach { it.execute(this) }
  override fun getTransition(event: T) = transitions.firstOrNull { it.isTransitionable(event) }
}
