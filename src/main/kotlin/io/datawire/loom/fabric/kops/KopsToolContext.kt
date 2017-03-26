package io.datawire.loom.fabric.kops


/**
 * Runtime configuration information needed for [KopsTool] to run.
 *
 * @property stateStorageBucket S3 bucket where cluster configuration is stored.
 */
data class KopsToolContext(val stateStorageBucket: String)
