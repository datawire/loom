package io.datawire.loom.proto.fabric.state.api


/**
 * Interface for a state implementations in the [StateMachine]. Every state should have a name and some operation that
 * occurs when the [enter] method is invoked.
 */
interface State<T> {

  /**
   * The name of the state. This must be unique when added to the [StateMachine].
   */
  val name: String

  /**
   * The available transitions that this state can enter.
   */
  val transitions: List<Transition<T>>

  /**
   * The action(s) to perform when the state is entered. Implementations of this interface should take care to ensure
   * the [enter] operations are idempotent.
   */
  fun enter()

  /**
   * Return the [Transition] for a given event.
   */
  fun getTransition(event: T): Transition<T>?

  /**
   * Indicates if the state does have any available transitions.
   */
  fun isTerminal() = transitions.isEmpty()

  /**
   * Indicates if the state does not have any available transitions.
   */
  fun isNotTerminal() = transitions.isNotEmpty()
}

/**
 * Implements the [State] interface in a way that makes it easy to use for subclasses.
 */
class AbstractState<T: Any>(override val name        : String,
                            override val transitions : List<Transition<T>>,
                            private  val actions     : List<(State<*>) -> Unit>) : State<T> {

  constructor(name: String, transitions: List<Transition<T>>, action: (State<*>) -> Unit)
      : this(name, transitions, listOf(action))

  override fun enter() = actions.forEach { it(this) }
  override fun getTransition(event: T) = transitions.firstOrNull { it.isTransitionable(event) }
}
