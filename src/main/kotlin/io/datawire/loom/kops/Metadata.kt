package io.datawire.loom.kops

import com.fasterxml.jackson.annotation.JsonFormat
import io.datawire.loom.core.Yaml
import java.time.Instant


data class Metadata(
    val name: String,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    val creationTimestamp: Instant,

    val labels: Map<String, String>
)

fun main(args: Array<String>) {
  val mt = Metadata("foobar", Instant.now(), emptyMap())
  println(Yaml().write(mt))
}