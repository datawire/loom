package io.datawire.loom.fabric.state

import org.slf4j.LoggerFactory

object TransitionLogger : TransitionAction {

  private val logger = LoggerFactory.getLogger("state-logger")

  override fun execute(source: State<*>, target: State<*>, transition: String) {
    logger.info("Transition['$transition'] ${source.name} -> ${target.name}")
  }
}