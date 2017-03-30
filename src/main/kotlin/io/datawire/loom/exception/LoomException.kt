package io.datawire.loom.exception

import org.apache.commons.lang3.text.WordUtils
import java.util.*


open class LoomException(val id  : UUID       = UUID.randomUUID(),
                         message : String?    = null,
                         cause   : Throwable? = null) : RuntimeException(formatMessage(id, message), cause)


private fun formatMessage(id: UUID, message: String?): String {
    val beginning = """$id

Exception while Loom was performing an operation. See the${ message?.let { " message and " } ?: " " }stacktrace below for details.
"""

    return message?.let { """$beginning

Message:
--------
${WordUtils.wrap(message, 100)}
"""
    } ?: beginning
}
