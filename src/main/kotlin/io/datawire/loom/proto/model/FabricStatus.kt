package io.datawire.loom.proto.model


enum class FabricStatus {
    NOT_STARTED,
    TERRAFORM_RUNNING,
    KOPS_RUNNING,
    COMPLETED,
    FAILED
}