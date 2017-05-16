package io.datawire.loom.core.validation

import com.fasterxml.jackson.core.JsonPointer


data class ValidationIssue(
    val title: String,
    val description: String,
    val path: String
)

fun buildNullIssue(path: JsonPointer) = ValidationIssue(
    title       = "Required Field Is Null or Absent",
    description = "JSON value at '$path' is required to be non-null but the provided value was null.",
    path        = path.toString()
)

fun fieldRegexMatchFailed(regex: Regex, vararg fieldPath: String) = fieldRegexMatchFailed(regex, fieldPath.toList())

fun fieldRegexMatchFailed(regex: Regex, fieldPath: List<String>): ValidationIssue {
  return ValidationIssue(
      title       = "Field does not match pattern",
      description = "JSON field '${fieldPath.joinToString(".", "{} -> ")}' is does not match acceptable pattern '$regex'.",
      path        = fieldPath.joinToString(".")
  )
}

fun fieldIsNull(vararg fieldPath: String) = fieldIsNull(fieldPath.toList())

fun fieldIsNull(fieldPath: List<String>): ValidationIssue {
  return ValidationIssue(
      title       = "Non-nullable field is null",
      description = "JSON field '${fieldPath.joinToString(".", "{} -> ")}' is null but required non-null value.",
      path        = fieldPath.joinToString(".")
  )
}

fun fieldIsIncorrectType(actualType: String, expectedType: String, vararg fieldPath: String) =
    fieldIsIncorrectType(actualType, expectedType, fieldPath.toList())

fun fieldIsIncorrectType(actualType: String, expectedType: String, fieldPath: List<String>): ValidationIssue {
  return ValidationIssue(
      title       = "Field is incorrect type",
      description = "JSON field '${fieldPath.joinToString(".", "{} -> ")}' is type <$actualType> but expected <$expectedType>",
      path        = fieldPath.joinToString(".")
  )
}
