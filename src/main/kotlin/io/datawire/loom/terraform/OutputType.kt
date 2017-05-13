package io.datawire.loom.terraform

import com.fasterxml.jackson.annotation.JsonProperty


enum class OutputType {
  @JsonProperty("string") STRING,
  @JsonProperty("list")   LIST,
  @JsonProperty("map")    MAP
}
