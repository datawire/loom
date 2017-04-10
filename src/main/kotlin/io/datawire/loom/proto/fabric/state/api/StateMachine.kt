package io.datawire.loom.proto.fabric.state.api


data class StateMachine<in T>(private val name         : String,
                              private val states       : Map<String, State<T>>) {

  constructor(name: String, states: Set<State<T>>): this(name, states.associateBy { it.name })

  private lateinit var currentState: State<T>

  private fun getState(name: String): State<T>? = states[name]

  val stateNames get() = states.keys

  fun start(stateName: String) {
    currentState = getState(stateName) ?: throw IllegalStateException("State named '$stateName' not found")
    currentState.enter()
  }

  fun process(event: T) {
    val transition = currentState.getTransition(event)
    currentState = transition?.let { trans ->

      trans.enter(currentState) { enteredState -> getState(enteredState)
          ?: throw IllegalStateException("State named '$enteredState' not found") }

    } ?: throw IllegalStateException("Transition for event '$event' on state named '${currentState.name}' not found")

    currentState.enter()
  }
}
