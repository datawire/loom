package io.datawire.loom.terraform

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.datawire.loom.terraform.jackson.ResourceBlockDeserializer
import io.datawire.loom.terraform.jackson.ResourceBlockSerializer


@JsonDeserialize(using = ResourceBlockDeserializer::class)
@JsonSerialize(using = ResourceBlockSerializer::class)
data class ResourceBlock(val resources: Map<String, Map<String, Resource>>)

fun create(resources: List<Resource>): ResourceBlock {
  val result = mutableMapOf<String, Map<String, Resource>>()

  for (r in resources) {
    if (r.type !in result) {
      result.put(r.type, mapOf(r.name to r))
    } else {
      val resourcesOfType = result[r.type]!!
      result.replace(r.type, resourcesOfType + Pair(r.name, r))
    }
  }

  return ResourceBlock(result)
}