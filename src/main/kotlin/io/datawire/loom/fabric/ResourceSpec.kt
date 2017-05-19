package io.datawire.loom.fabric

import io.datawire.loom.terraform.Module
import io.datawire.loom.terraform.TerraformValue


data class ResourceSpec(
    val name       : String,
    val model      : String,
    val source     : String,
    val parameters : Map<String, TerraformValue<*>>
) {

  fun toTerraformModule() = Module(name = name, source = source, variables = parameters)
}

fun assemble(fabric: FabricSpec, model: ResourceModel, config: ResourceConfig): ResourceSpec {

  val params = model.variables.mapValues { (variable, model) ->
    val variableValue = config.parameters[variable]
    if (variableValue == null && model.required) {
      throw IllegalArgumentException(
          "Model indicates variable ('$variable') is required, but it's not present in the config.")
    }

    variableValue ?: model.type.getNullValue()
  }

  return ResourceSpec(
      name       = config.name,
      model      = model.name,
      source     = model.source,
      parameters = params
  )
}