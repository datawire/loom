package io.datawire.loom.terraform

import com.fasterxml.jackson.module.kotlin.readValue
import io.datawire.loom.core.Json


data class Resource(
    val type       : String,
    val name       : String,
    val properties : Map<String, TerraformValue<*>> = emptyMap()
)

fun main(args: Array<String>) {

//  val template = terraformTemplate(
//      resources = listOf(
//          Resource("aws_instance", "web", mapOf("name" to TerraformString("barbazbot"), "connection" to TerraformMap(mapOf("ssh" to "beepboop")))),
//          Resource("aws_instance", "blap", mapOf("name" to TerraformString("fizzbz"), "connection" to TerraformMap(mapOf("ssh" to "beepboop")))),
//          Resource("aws_elb", "blapper", mapOf("name" to TerraformString("barbazbot"), "connection" to TerraformMap(mapOf("ssh" to "snorksnork"))))
//      )
//  )
//
//  println(template.render())


  val json = """
{
  "resource" : {
    "aws_instance" : {
      "web" : {
        "name" : "barbazbot",
        "connection" : {
          "ssh" : "beepboop"
        }
      },
      "blap" : {
        "name" : "fizzbz",
        "connection" : {
          "ssh" : "beepboop"
        }
      }
    },
    "aws_elb" : {
      "blapper" : {
        "name" : "barbazbot",
        "connection" : {
          "ssh" : "snorksnork"
        }
      }
    }
  }
}
"""

  val block = Json().mapper.readValue<ResourceBlock>(json)
  println(block)
}