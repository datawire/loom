package io.datawire.loom.dev.core

import com.fasterxml.jackson.databind.JsonNode


class FabricValidator(private val models: FabricModelDao) {

  fun validate(json: JsonNode) {
    json.get("")
  }


}