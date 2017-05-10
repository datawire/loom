package io.datawire.loom.dev.model.terraform

import com.fasterxml.jackson.databind.ObjectMapper
import io.datawire.loom.proto.fabric.terraform.TfTemplate
import java.nio.file.Files
import java.nio.file.Path


class TerraformProject(
    private val jsonMapper: ObjectMapper,
    private val terraformWorkspace: Path,
    private val fabricName: String
) {

  private val terraformOverrideFile = terraformWorkspace.resolve("override.tf.json").toFile()

  fun createOverride(template: TfTemplate) {
    jsonMapper.writeValue(terraformOverrideFile, template)
  }

  fun deleteOverride() {
    Files.deleteIfExists(terraformOverrideFile.toPath())
  }
}
