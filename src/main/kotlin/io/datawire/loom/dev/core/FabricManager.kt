package io.datawire.loom.dev.core

import io.datawire.loom.dev.core.kops.Kops
import io.datawire.loom.dev.core.terraform.Terraform
import io.datawire.loom.dev.model.FabricConfig
import java.nio.file.Path


class FabricManager(
    private val workspace : Path,
    private val terraform : Terraform,
    private val kops      : Kops
) {

  fun setup(config: FabricConfig) {

  }

  fun delete(name: String) {

  }

  fun deleteCluster(name: String) {

  }
}