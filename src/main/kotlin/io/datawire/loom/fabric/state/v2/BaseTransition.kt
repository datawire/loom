package io.datawire.loom.fabric.state.v2

import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class BaseTransition<T: Event>(override val name: String,
                                        val target: String) : Transition<T> {

  protected val logger: Logger = LoggerFactory.getLogger(javaClass)

  open fun onEnter() = logger.info("Transition -> $target")

  override fun enter(lookupTarget: (String) -> State<T>?): State<T>? {
    onEnter()
    return lookupTarget(target)
  }
}