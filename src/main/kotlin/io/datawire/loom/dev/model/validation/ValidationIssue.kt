package io.datawire.loom.dev.model.validation


data class ValidationIssue(
    val title: String,
    val description: String,
    val path: String
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