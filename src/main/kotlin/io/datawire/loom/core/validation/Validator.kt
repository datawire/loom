package io.datawire.loom.core.validation

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode

abstract class Validator {
  abstract fun validate(root: JsonNode)
  protected fun path(path: String): JsonPointer = JsonPointer.compile(path)
}
