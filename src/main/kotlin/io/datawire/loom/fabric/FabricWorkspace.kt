package io.datawire.loom.fabric

import com.fasterxml.jackson.module.kotlin.readValue
import io.datawire.loom.core.Json
import io.datawire.loom.terraform.Template
import io.datawire.loom.terraform.terraformTemplate
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.Paths
import java.lang.System.*
import java.nio.file.Files

/**
 * Interface for interacting with the filesystem where fabric configuration is manipulated and stored for processing.
 *
 * @property name the name of the fabric.
 * @property home the path to the workspace which will become $HOME when used by external processes.
 */
class FabricWorkspace(val name: String, val home: Path) {

  private val json = Json()

  val environment = mapOf<String, String>("HOME" to home.toAbsolutePath().toString())

  val terraform: Path = createDirectories(resolve("terraform"))

  fun loadTerraformTemplate(): Template? =
      try { json.read(terraform.resolve("loom.tf.json")) } catch (ioe: IOException) { null }

  fun exists(path: String): Boolean = exists(this.home.resolve(path))

  fun resolve(path: String): Path = this.home.resolve(path)

  fun resolve(path: Path): Path = this.home.resolve(path)

  fun write(path: String, data: String) { write(resolve(path), data.toByteArray()) }

  fun write(path: Path, data: String) { write(resolve(path), data.toByteArray()) }

  fun read(path: Path) = newBufferedReader(resolve(path)).readText()
}

/**
 * Create a new [FabricWorkspace] with the given name and in a directory child of [parent].
 *
 * @param fabricName the name of the fabric.
 * @param parent the parent directory that will contain the workspace directory.
 * @return a [FabricWorkspace] instance.
 */
fun newWorkspace(
    fabricName : String,
    parent     : Path = Paths.get(getProperty("user.home"), "loom-workspace", "fabrics")
): FabricWorkspace {
  val workspace = createDirectories(parent.resolve(fabricName))

  val aws = ".aws"
  if (isDirectory(parent.resolve(aws)) && !isSymbolicLink(workspace.resolve(aws))) {
    createSymbolicLink(workspace.resolve(aws), parent.resolve(aws))
  }

  return FabricWorkspace(fabricName, workspace)
}
