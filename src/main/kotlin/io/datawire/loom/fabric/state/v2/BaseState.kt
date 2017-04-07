package io.datawire.loom.fabric.state.v2

import org.slf4j.Logger
import org.slf4j.LoggerFactory


abstract class BaseState<T: Event>(override val name: String,
                                   override val transitions: Map<T, Transition<T>>) : State<T> {

  protected val logger: Logger = LoggerFactory.getLogger(javaClass)

  open fun onEnter() = logger.info("state '{}'", name)

  override final fun enter() = onEnter()
}