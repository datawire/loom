package io.datawire.loom.terraform

import com.fasterxml.jackson.annotation.JsonProperty


enum class TerraformType {
  @JsonProperty("string") STRING,
  @JsonProperty("list")   LIST,
  @JsonProperty("map")    MAP;

  fun getNullValue(): TerraformValue<*> {
    return when(this) {
      TerraformType.STRING -> TerraformString("")
      TerraformType.LIST   -> TerraformList(emptyList())
      TerraformType.MAP    -> TerraformMap(emptyMap())
    }
  }
}