package io.datawire.loom.fabric


import io.datawire.loom.terraform.Module
import io.datawire.loom.terraform.TerraformValue


data class ResourceSpec(
    val name: String,
    val model: String,
    val source: String,
    val parameters: Map<String, TerraformValue<*>>
) {

  fun toTerraformModule() = Module(name = name, source = source, variables = parameters)
}

fun assemble(fabric: FabricSpec, model: ResourceModel, config: ResourceConfig): ResourceSpec {
  val parameters = model.variables.mapValues { (variableName, variable) ->
    val candidateValue = config.parameters[variableName]

    candidateValue
        ?: if (!variable.required) variable.type.getNullValue()
           else throw IllegalArgumentException("Model indicates variable ('$variable') is required, but it's not present in the config.")
  }

  return ResourceSpec(
      name       = config.name,
      model      = model.name,
      source     = model.source,
      parameters = parameters
  )
}