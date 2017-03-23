package io.datawire.loom.v1

import io.vertx.core.Launcher
import java.io.FileInputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

private val MAIN_CLASS = Loom::class.qualifiedName!!

object Runner {
    fun run(arguments: Array<String>) {
        val launcher = Launcher()
        launcher.dispatch(this, arguments)
    }
}

fun main(arguments: Array<String>) {
    configureProperties()

    val loomExternalToolsPath = Paths.get(System.getProperty("loom.external-tools.path", "./.loom/bin"))
    Files.createDirectories(loomExternalToolsPath)

    Runner.run(configureArguments(arguments))
}

private fun configureProperties() {
    val props = Properties()
    props.load(FileInputStream("config/server.properties"))
    for ((name, value) in props) {
        System.setProperty(name.toString(), value.toString())
    }
}

private fun configureArguments(original: Array<String>): Array<String> {
    val result = original.toMutableSet()

    if (result.isEmpty()) {
        result += setOf("run", "-conf", "config/loom.json", MAIN_CLASS)
    }

    return result.toTypedArray()
}
