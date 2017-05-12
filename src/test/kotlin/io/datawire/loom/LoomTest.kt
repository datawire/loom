package io.datawire.loom

import java.nio.file.Path
import java.nio.file.Paths

abstract class LoomTest {

  val homeDirectory: Path = Paths.get(System.getProperty("user.dir"))
}