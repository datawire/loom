package io.datawire.loom.exception

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "error")
@JsonSubTypes(
        JsonSubTypes.Type(value = ModelNotFound::class,  name = "MODEL_NOT_FOUND"),
        JsonSubTypes.Type(value = FabricNotFound::class, name = "FABRIC_NOT_FOUND")
)
sealed class NotFound(val id: String)

class ModelNotFound(id: String) : NotFound(id)
class FabricNotFound(id: String) : NotFound(id)
