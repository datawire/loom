package io.datawire.loom.dev.core

import java.nio.file.Files
import java.nio.file.Path


class FileSystemFabricWorkspace(override val path: Path): AutoCloseable, Workspace {

  override fun delete() {
    deleteDirectory(path)
  }

  override fun createDirectory(name: String) {
    createDirectory(name, { })
  }

  override fun createDirectory(name: String, after: Path.() -> Unit) {
    val path = Files.createDirectories(path.resolve(name))
    after(path)
  }

  override fun close() {
    delete()
  }

  private fun deleteFile(path: Path) {
    if (Files.isRegularFile(path)) {
      Files.deleteIfExists(path)
    } else {
      throw IllegalArgumentException("Path '$path' is not a file.")
    }
  }

  private fun deleteDirectory(path: Path) {
    if (Files.isDirectory(path)) {
      Files.walk(path).sorted(Comparator.reverseOrder()).forEach { Files.deleteIfExists(it) }
    } else {
      throw IllegalArgumentException("Path '$path' is not a directory.")
    }
  }
}