package io.datawire.loom.core

import java.io.Closeable
import java.nio.file.Files
import java.nio.file.Path


abstract class Workspace(val path: Path): Closeable {

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