package io.datawire.loom.proto.fabric.state.api

import org.slf4j.LoggerFactory

object TransitionLogger : TransitionAction {

  private val logger = LoggerFactory.getLogger("state-logger")

  override fun execute(source: String, target: String, transition: String) {
    logger.info("Transition: $transition")
  }
}