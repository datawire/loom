package io.datawire.loom


import io.datawire.loom.config.LoomConfig
import io.datawire.loom.data.fromYaml
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.*


fun main(args: Array<String>) {
    configureProperties()

    val configFile = if (args.isNotEmpty()) Paths.get(args[0]) else Paths.get("config/loom.json")
    val config = fromYaml<LoomConfig>(configFile)
    Loom(config).run()
}

private fun configureProperties() {
    val props = Properties()
    props.load(FileInputStream("config/server.properties"))
    for ((name, value) in props) {
        System.setProperty(name.toString(), value.toString())
    }
}
