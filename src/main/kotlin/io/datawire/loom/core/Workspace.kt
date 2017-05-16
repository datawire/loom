package io.datawire.loom.core

import java.io.Closeable
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


open class Workspace(val path: Path): Closeable {

  init {
    if (!Files.isDirectory(path)) {
      throw IllegalArgumentException("Workspace path '$path' is not a directory")
    }
  }

  fun delete() {
    if (Files.isDirectory(path)) {
      Files.walk(path).sorted(Comparator.reverseOrder()).forEach { Files.deleteIfExists(it) }
    }
  }

  override fun close() = delete()
}

fun newWorkspace(name: String): Workspace {
  val userHome = Paths.get(System.getProperty("user.home"))
  val workspace = Files.createDirectories(userHome.resolve("loom-workspace").resolve("fabrics").resolve(name))

  // If the user is using explicit AWS credentials and config and therefore not using IAM and probably running locally
  // then symlink the running users '${HOME}/.aws' directory to the fabric workspace so that tools which operate in the
  // workspace and require AWS connectivity operate as expected.
  if (Files.isDirectory(userHome.resolve(".aws")) && !Files.isSymbolicLink(workspace.resolve(".aws"))) {
    Files.createSymbolicLink(workspace.resolve(".aws"), userHome.resolve(".aws"))
  }

  return Workspace(workspace)
}
