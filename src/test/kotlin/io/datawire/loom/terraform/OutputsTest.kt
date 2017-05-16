package io.datawire.loom.terraform

import io.datawire.loom.core.Json
import org.junit.Test

import org.assertj.core.api.Assertions.*


class OutputsTest {

  @Test
  fun deserialize() {
    val json = """{
    "random_name": {
        "sensitive": false,
        "type": "string",
        "value": "arriving-sponge"
    }
}"""

    val outputs = Json().read<Outputs>(json)

    assertThat(outputs.size).isEqualTo(1)

    val output = outputs.getOutput("random_name")
    assertThat(output).isNotNull()
    assertThat(output?.sensitive).isFalse()
    assertThat(output?.type).isEqualTo(OutputType.STRING)
    assertThat(output?.value?.value).isEqualTo("arriving-sponge")
  }
}