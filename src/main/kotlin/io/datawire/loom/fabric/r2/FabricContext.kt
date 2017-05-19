package io.datawire.loom.fabric.r2

import io.datawire.loom.fabric.FabricSpec


data class FabricContext(
    val fabricService : FabricService,
    val fabricSpec    : FabricSpec
)
