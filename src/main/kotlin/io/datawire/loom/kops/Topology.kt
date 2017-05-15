package io.datawire.loom.kops

import com.fasterxml.jackson.annotation.JsonProperty


enum class Topology {
  @JsonProperty("Public") PUBLIC,
  @JsonProperty("Private") PRIVATE
}