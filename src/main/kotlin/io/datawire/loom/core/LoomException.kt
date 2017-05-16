package io.datawire.loom.core


class LoomException(val statusCode: Int = 500) : RuntimeException()