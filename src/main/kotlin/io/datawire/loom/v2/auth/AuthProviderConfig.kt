package io.datawire.loom.v2.auth

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(value = NoAuthProvider::class, name = "none")
)
interface AuthProviderConfig {
    fun configure(router: Router)
}

class NoAuthProvider : AuthProviderConfig {
    override fun configure(router: Router) { }
}