package io.datawire.loom.exception


class NotFoundException(val notFound: NotFound) : RuntimeException()