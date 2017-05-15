package io.datawire.loom.kops

import com.fasterxml.jackson.annotation.JsonProperty


enum class InstanceRole {
  @JsonProperty("Master") MASTER,
  @JsonProperty("Node")   NODE
}
