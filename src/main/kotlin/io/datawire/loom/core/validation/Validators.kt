package io.datawire.loom.core.validation


import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeType


private fun JsonNode.isNullOrAbsent() = this.isNull || this.isMissingNode

fun JsonNode.validate(
    field    : JsonPointer,
    nullable : Boolean = false,
    type     : JsonNodeType? = null,
    check    : JsonNode.() -> Boolean,
    failed   : JsonNode.(field: JsonPointer) -> ValidationIssue): ValidationIssue? {

  val node = at(field)
  if (!nullable && node.isNullOrAbsent()) {
    return buildNullIssue(field)
  } else if (nullable && node.isNullOrAbsent()) {
    return null
  }

  if (!node.isNullOrAbsent() && type != node.nodeType) {
    return ValidationIssue(
        title       = "Value Is Wrong Type",
        description = "Value is expected to be $type but was ${node.nodeType}",
        path        = field.toString()
    )
  }

  return if(!check(this.at(field))) failed(field) else null
}

fun JsonNode.matches(field: JsonPointer, regex: Regex)
    = validate(field, false, JsonNodeType.STRING, { regex.matches(textValue()) }, issue("Value Does Not Match Regex Pattern", "Value did not match the expected regular expression: $regex"))

fun field(text: String): JsonPointer = JsonPointer.compile(text)

fun issue(title: String, description: String): JsonNode.(path: JsonPointer) -> ValidationIssue {
  return { ValidationIssue(title, description, it.toString()) }
}
