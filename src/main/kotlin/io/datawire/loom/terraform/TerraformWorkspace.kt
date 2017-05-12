package io.datawire.loom.terraform

import com.fasterxml.jackson.module.kotlin.readValue
import io.datawire.loom.core.Json
import io.datawire.loom.core.Workspace
import java.nio.file.Files
import java.nio.file.Path


class TerraformWorkspace(path: Path) : Workspace(path) {

  private val providerPath = path.resolve("override.tf.json")
  private val backendPath  = path.resolve("backend.tf.json")
  private val loomPath     = path.resolve("loom.tf.json")

  fun configureProvider(provider: Provider) {

    // We put provider configuration in "override.tf.json" because sometimes Terraform modules do bad things such as
    // configure there own provider block that then conflicts with the main provider block and causes errors.

    terraformTemplate(providers = listOf(provider)).render(path.resolve(providerPath))
  }

  fun configureBackend(backend: Backend) {
    terraformTemplate(terraform = terraformBlock(backend)).render(path.resolve(backendPath))
  }

  fun configureTemplate(template: Template) = template.render(path.resolve(loomPath))

  fun fetchProvider(): Provider? {
    return if (Files.isReadable(providerPath)) {
      Json.mapper.readValue<Template>(providerPath.toFile()).providers["aws"]
    } else {
      null
    }
  }

  fun fetchBackend(): Backend? {
    return if (Files.isReadable(backendPath)) {
      Json.mapper.readValue<Template>(backendPath.toFile()).terraform!!.backend["s3"]
    } else {
      null
    }
  }

  fun fetchTemplate(): Template? {
    return if (Files.isReadable(loomPath)) {
      Json.mapper.readValue<Template>(loomPath.toFile())
    } else {
      null
    }
  }

  fun resolve(path: String): Path = this.path.resolve(path)
}