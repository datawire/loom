package io.datawire.loom.proto.fabric.kops

import java.nio.file.Path


/**
 * Runtime configuration information needed for [KopsTool] to run.
 *
 * @property stateStorageBucket S3 bucket where cluster configuration is stored.
 * @property workspace File system path where Kops should execute.
 */
data class KopsToolContext(val stateStorageBucket: String, val workspace: Path)
