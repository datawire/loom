package io.datawire.loom.dev.core

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


interface Workspace {

  val path: Path

  fun terraformWorkspace(): Path = path.resolve("terraform")

  fun createDirectory(name: String)

  fun createDirectory(name: String, after: Path.() -> Unit)

  fun delete()
}

fun createWorkspace(fabricName: String): Workspace {
  val userHome = Paths.get(System.getProperty("user.home"))

  val fabricWorkspace = Files.createDirectories(userHome.resolve("loom").resolve("fabrics").resolve(fabricName))

  // If the user is using explicit AWS credentials and config and therefore not using IAM and probably running locally
  // then symlink the running users '${HOME}/.aws' directory to the fabric workspace so that tools which operate in the
  // workspace and require AWS connectivity operate as expected.
  userHome.resolve(".aws").ifDirectory { Files.createSymbolicLink(fabricWorkspace, this) }

  return FileSystemFabricWorkspace(fabricWorkspace)
}

private fun Path.ifDirectory(then: Path.() -> Unit) {
  if (Files.isDirectory(this)) {
    then(this)
  }
}
