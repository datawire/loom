package io.datawire.loom.core.validation


class ValidationException(val issues: List<ValidationIssue>) : RuntimeException()
