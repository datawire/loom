package io.datawire.loom.fabric

import io.datawire.loom.core.Json
import io.datawire.loom.terraform.Template
import io.datawire.loom.terraform.TemplateView
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.Paths
import java.lang.System.*

/**
 * Interface for interacting with the filesystem where fabric configuration is manipulated and stored for processing.
 *
 * @property fabricName the name of the fabric.
 * @property home the path to the workspace which will become $HOME when used by external processes.
 */
class FabricWorkspace(val fabricName: String, val home: Path) {

  private val log = LoggerFactory.getLogger(FabricWorkspace::class.java)

  private val json = Json()

  val environment = mapOf<String, String>("HOME" to home.toAbsolutePath().toString())

  val terraform: Path = createDirectories(resolve("terraform"))

  val kops: Path = createDirectories(resolve("kops"))

  fun loadTerraformTemplate(): Template? =
      try { json.read(read(resolve("terraform/main.tf.json"))) } catch (ioe: IOException) { null }

  fun exists(path: String): Boolean = exists(this.home.resolve(path))

  fun resolve(path: String): Path = this.home.resolve(path)

  fun resolve(path: Path): Path = this.home.resolve(path)

  fun writeTerraformTemplate(name: String, template: Template, path: Path = terraform) {
    val terraformJson = json.writeUsingView<TemplateView>(template)
    write(path.resolve("$name.tf.json"), terraformJson)
  }

  fun write(path: String, data: String) { write(Paths.get(path), data.toByteArray()) }

  fun write(path: Path, data: String) {
    write(resolve(path), data.toByteArray())
    log.debug("""Wrote data to file: '{}'
{}""", path, data)
  }

  fun read(path: Path): String {
    val result = newBufferedReader(resolve(path)).readText()
    log.debug("""Read data from file: '{}'" +
{}
""", path, result)

    return result
  }
}

/**
 * Create a new [FabricWorkspace] with the given name and in a directory child of [parent].
 *
 * @param fabricName the name of the fabric.
 * @param parent the parent directory that will contain the workspace directory.
 * @return a [FabricWorkspace] instance.
 */
fun getOrCreateWorkspace(
    fabricName : String,
    parent     : Path = Paths.get(getProperty("user.home"), "loom-workspace", "fabrics")
): FabricWorkspace {
  val realHome  = Paths.get(getProperty("user.home"))
  val workspace = createDirectories(parent.resolve(fabricName))

  val aws = ".aws"
  if (isDirectory(realHome.resolve(aws)) && !isSymbolicLink(workspace.resolve(aws))) {
    createSymbolicLink(workspace.resolve(aws), realHome.resolve(".aws"))
  }

  return FabricWorkspace(fabricName, workspace)
}
