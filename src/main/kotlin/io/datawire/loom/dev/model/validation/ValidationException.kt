package io.datawire.loom.dev.model.validation


class ValidationException(val issues: List<ValidationIssue>) : RuntimeException()
