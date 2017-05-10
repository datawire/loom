package io.datawire.loom.dev.model.validation

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeType


/**
 * Check whether the given [JsonNode] is "null" due to either being explicitly set as "null" or just absent from the
 * JSON.
 */
private fun JsonNode.isNullOrAbsent() = this.isNull || this.isMissingNode

fun buildNullIssue(path: JsonPointer) = ValidationIssue(
    title       = "Required Field Is Null or Absent",
    description = "JSON value at '$path' is required to be non-null but the provided value was null.",
    path        = path.toString()
)

private fun belowMinIssue(path: JsonPointer) = ValidationIssue(
    title       = "Value Is Too Low",
    description = "JSON value at '$path' is required to be greater than but was ",
    path        = path.toString()
)

/**
 *
 */
fun check(
    root: JsonNode,
    path: JsonPointer,
    nullable: Boolean = false,
    expectedType: JsonNodeType,
    block: (JsonNode, JsonPointer) -> ValidationIssue?): ValidationIssue? {

  val nodeValue = root.at(path)
  if (!nullable && nodeValue.isNullOrAbsent()) {
    return buildNullIssue(path)
  }

  if (!nodeValue.isNullOrAbsent() && expectedType != nodeValue.nodeType) {
    return ValidationIssue(
        title       = "Field Is Wrong Type",
        description = "JSON value at '$path' is expected to be $expectedType but was ${nodeValue.nodeType}",
        path        = path.toString()
    )
  }

  return block(nodeValue, path)
}

/**
 *
 */
fun checkMatches(root: JsonNode, path: JsonPointer, regex: Regex, nullable: Boolean = false): ValidationIssue? {
  return check(root, path, nullable, JsonNodeType.STRING) { node, path ->
    if (!regex.matches(node.textValue())) {
      ValidationIssue(
          title       = "Field Does Not Match Regex Pattern",
          description = "JSON value at '$path' did not match expected pattern /$regex/",
          path        = path.toString()
      )
    } else {
      null
    }
  }
}

/**
 * Check if the value referenced by the given [JsonPointer] is not null or missing.
 *
 * @param json a constructed [JsonNode] to operate on.
 * @param path path within the [JsonNode] to inspect.
 */
//fun checkRequired(json: JsonNode, path: JsonPointer): ValidationIssue? {
//  return if (json.at(path).isNullOrAbsent()) {
//    buildNullIssue(path)
//  } else {
//    null
//  }
//}

/**
 * Check if the value referenced by the given [JsonPointer] is not null or missing.
 *
 * @param json a constructed [JsonNode] to operate on.
 * @param path path within the [JsonNode] to inspect.
 * @param expectedType expected type of the value for the [JsonNode].
 * @param nullable whether the value is allowed to be null or not.
 */
//fun checkType(json: JsonNode, path: JsonPointer, expectedType: JsonNodeType, nullable: Boolean = false): ValidationIssue? {
//  val nodeValue = json.at(path)
//
//  if (!nullable && nodeValue.isNullOrAbsent()) {
//    return buildNullIssue(path)
//  }
//
//  return if (!nodeValue.isNullOrAbsent() && expectedType != nodeValue.nodeType) {
//    ValidationIssue(
//        title       = "Field Is Wrong Type",
//        description = "JSON value at '$path' is expected to be $expectedType but was ${nodeValue.nodeType}",
//        path        = path.toString()
//    )
//  } else {
//    null
//  }
//}

/**
 * Check if the value referenced by the given [JsonPointer] matches the provided regular expression.

 * @param json a constructed [JsonNode] to operate on.
 * @param path path within the [JsonNode] to inspect.
 * @param regex the pattern to match the value against.
 * @param nullable whether the value is allowed to be null or not.
 */
//fun checkMatches(json: JsonNode, path: JsonPointer, regex: Regex, nullable: Boolean = false): ValidationIssue? {
//  val nodeValue = json.at(path)
//
//  val typeCheckIssue = checkType(nodeValue, path, JsonNodeType.STRING, nullable)
//  if (typeCheckIssue != null) {
//    return typeCheckIssue
//  }
//
//  return if (!regex.matches(nodeValue.textValue())) {
//    ValidationIssue(
//        title       = "Field Does Not Match Regex Pattern",
//        description = "JSON value at '$path' did not match expected pattern /$regex/",
//        path        = path.toString()
//    )
//  } else {
//    null
//  }
//}

//fun check(json: JsonNode, path: JsonPointer, nullable: Boolean = false, expectedType: JsonNodeType? = null,
//          check: (JsonPointer, JsonNode) -> ValidationIssue?): ValidationIssue? {
//
//  val nodeValue = json.at(path)
//  if (expectedType != null) {
//    val typeCheckIssue = checkType(nodeValue, path, JsonNodeType.STRING, nullable)
//    if (typeCheckIssue != null) {
//      return typeCheckIssue
//    }
//  }
//
//  return check(path, nodeValue)
//}

//fun checkRequired(json: JsonNode, path: String) = checkRequired(json, JsonPointer.compile(path))
//
//fun checkType(json: JsonNode, path: String, expectedType: JsonNodeType, nullable: Boolean = false)
//    = checkType(json, JsonPointer.compile(path), expectedType, nullable)
//
//fun checkMatches(json: JsonNode, path: String, regex: Regex, nullable: Boolean = false)
//    = checkMatches(json, JsonPointer.compile(path), regex, nullable)


//fun main(args: Array<String>) {
//  val json = JSON_MAPPER
//  val tree = json.readTree("""
//{
//  "foo": "bar",
//  "baz": null,
//  "snork": {
//    "blop": {"foo": "bar"}
//  }
//}
//""")
//
//  println(tree.at("/foo"))
//}