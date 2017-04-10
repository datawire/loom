package io.datawire.loom.proto.model


/**
 * Information about an error that occurred in Loom.
 *
 * @property code   unique code used by Loom that represents the type of error.
 * @property title  short description of the error that is always the same for [ErrorDetail] if `e1.code == e2.code`
 * @property detail detailed description of the error that can change depending on the context of the error.
 */
data class ErrorDetail(val code   : String,
                       val title  : String,
                       val detail : String)

/**
 * Produce a [Results] object for many [ErrorDetail] instances.
 */
fun errors(details: List<ErrorDetail>) = Results("errors", details)

/**
 * Produce a [Results] object for a single [ErrorDetail] instance.
 *
 * @see errors
 */
fun error(detail: ErrorDetail) = errors(listOf(detail))