package io.datawire.loom.fabric.state

interface Transition<T> {

  val name    : String

  fun isTransitionable(event: T): Boolean

  fun enter(source: State<T>, target: (String) -> State<T>): State<T>?
}

abstract class ActiveTransition<T: Any>(override val name     : String,
                                        protected val target  : State<T>,
                                        protected val actions : List<TransitionAction>) : Transition<T> {

  override fun enter(source: State<T>, target: (String) -> State<T>): State<T>? {
    val targetState = target(this.target.name)
    actions.forEach { it.execute(source, targetState, this.name) }
    return targetState
  }
}
