package io.datawire.loom.fabric.state

import org.slf4j.LoggerFactory

object StateLogger : StateAction {

  private val logger = LoggerFactory.getLogger("state-logger")

  override fun execute(state: State<*>) {
    logger.info("IN: ${state.name}")
  }
}