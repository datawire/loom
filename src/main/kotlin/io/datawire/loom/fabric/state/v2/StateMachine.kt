package io.datawire.loom.fabric.state.v2


data class StateMachine<in T: Event>(val name: String,
                                     private val states: Map<String, State<T>>) {

  private lateinit var currentState: State<T>

  constructor(name: String, states: Set<State<T>>): this(name, states.associateBy { it.name })

  private fun getState(name: String): State<T>? = states[name]

  fun process(event: T) {
    val transition = currentState.getTransition(event)

    currentState = transition?.let { trans ->

      val lookup = { entered: String -> getState(entered) ?: throw IllegalStateException("State '$entered' not found") }
      trans.enter(lookup)

    } ?: throw IllegalStateException("Transition for event '$event' on state named '${currentState.name}' not found")

    currentState.enter()
  }
}