package io.datawire.loom.model

import org.eclipse.jetty.http.HttpStatus.*


enum class ErrorInfo(val id: Int, val description: String, val httpStatusCode: Int) {

    // -----------------------------------------------------------------------------------------------------------------
    // Errors related to internal processing have the lowest range (1..999)
    // -----------------------------------------------------------------------------------------------------------------

    GENERAL_ERROR   (1, "Error of unspecified origin or type occurred", INTERNAL_SERVER_ERROR_500),
    NOT_IMPLEMENTED (2, "Unimplemented resource or operation", NOT_IMPLEMENTED_501),

    // -----------------------------------------------------------------------------------------------------------------
    // Fabric Model errors
    // -----------------------------------------------------------------------------------------------------------------

    MODEL_NOT_EXISTS (1000, "The fabric model does not exist", NOT_FOUND_404),
    MODEL_EXISTS     (1001, "The fabric model already exists", CONFLICT_409),
    MODEL_INVALID    (1002, "The fabric model definition provided is invalid", UNPROCESSABLE_ENTITY_422),

    // -----------------------------------------------------------------------------------------------------------------
    // Fabric errors
    // -----------------------------------------------------------------------------------------------------------------

    FABRIC_NOT_EXISTS (1100, "The fabric does not exist", NOT_FOUND_404),
    FABRIC_EXISTS     (1101, "The fabric already exists", CONFLICT_409),
    FABRIC_INVALID    (1102, "The fabric definition provided is invalid", UNPROCESSABLE_ENTITY_422),

    // -----------------------------------------------------------------------------------------------------------------
    // Cluster errors
    // -----------------------------------------------------------------------------------------------------------------

    CLUSTER_NOT_FOUND (1200, "The cluster does not exist", NOT_FOUND_404),
    CLUSTER_EXISTS    (1201, "The cluster already exists", NOT_FOUND_404);
}

fun lookupByException(ex: Exception): ErrorInfo {
    return when (ex) {
        is ResourceNotExistsException -> {
            when (ex.reference) {
                is ModelReference   -> ErrorInfo.MODEL_NOT_EXISTS
                is FabricReference  -> ErrorInfo.FABRIC_NOT_EXISTS
                is ClusterReference -> ErrorInfo.CLUSTER_NOT_FOUND
            }
        }
        is ResourceExistsException -> {
            when (ex.reference) {
                is ModelReference   -> ErrorInfo.MODEL_EXISTS
                is FabricReference  -> ErrorInfo.FABRIC_EXISTS
                is ClusterReference -> ErrorInfo.CLUSTER_EXISTS
            }
        }
        else -> ErrorInfo.GENERAL_ERROR
    }
}
