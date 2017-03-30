package io.datawire.loom.exception

import io.datawire.loom.model.ErrorDetail
import io.datawire.loom.model.Results
import io.datawire.loom.model.lookupByException
import org.apache.commons.lang3.text.WordUtils
import java.util.*


open class LoomException(val id  : UUID       = UUID.randomUUID(),
                         message : String?    = null,
                         cause   : Throwable? = null) : RuntimeException(formatMessage(id, message), cause) {

    fun getStatusCodeAndErrorDetails(): Pair<Int, Results<ErrorDetail>> {
        val info = lookupByException(this)
        return Pair(info.httpStatusCode, io.datawire.loom.model.error(ErrorDetail(info.id.toString(), info.name, info.description)))
    }
}


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
