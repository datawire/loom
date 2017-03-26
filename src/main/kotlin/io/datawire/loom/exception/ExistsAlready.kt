package io.datawire.loom.exception

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "error")
@JsonSubTypes(
        JsonSubTypes.Type(value = ModelNotFound::class,  name = "MODEL_EXISTS"),
        JsonSubTypes.Type(value = FabricNotFound::class, name = "FABRIC_EXISTS")
)
sealed class ExistsAlready(val id: String)

class ModelExists(id: String) : ExistsAlready(id)
class FabricExists(id: String) : ExistsAlready(id)
