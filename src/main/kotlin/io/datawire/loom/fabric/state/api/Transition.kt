package io.datawire.loom.fabric.state.api

interface Transition<T> {
  val name: String

  fun isTransitionable(event: T): Boolean

  fun enter(source: State<T>, lookupTarget: (String) -> State<T>): State<T>?
}

abstract class AbstractTransition<T>(override val name: String,
                                     val target: String,
                                     val checkTransitionable: (T) -> Boolean,
                                     val actions: List<(String, String, String) -> Unit>) : Transition<T> {

  constructor(name: String,
              target: String,
              checkTransitionable: (T) -> Boolean, action: (String, String, String) -> Unit)
      : this(name, target, checkTransitionable, listOf(action))

  override fun isTransitionable(event: T) = checkTransitionable(event)

  override fun enter(source: State<T>, lookupTarget: (String) -> State<T>): State<T>? {
    actions.forEach { it(source.name, target, this.name) }
    return lookupTarget(target)
  }
}

fun <T> transition(source: String,
                   target: String,
                   checkTransitionable: (T) -> Boolean,
                   action: ((String, String, String) -> Unit)? = null): Transition<T> {

  return object : AbstractTransition<T>(
      name                = "$source -> $target",
      target              = target,
      checkTransitionable = checkTransitionable,
      actions             = action?.let { listOf(it) } ?: emptyList()
  ) {}
}
