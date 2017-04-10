package io.datawire.loom.proto.fabric.state.api

import org.slf4j.LoggerFactory

object StateLogger : StateAction<State<*>> {

  private val logger = LoggerFactory.getLogger("state-logger")

  override fun execute(state: State<*>) {
    logger.info("IN: ${state.name}")
  }
}