package io.datawire.loom.terraform

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.datawire.loom.core.Json
import io.datawire.loom.terraform.jackson.ModuleDeserializer
import io.datawire.loom.terraform.jackson.OutputDeserializer
import io.datawire.loom.terraform.jackson.ProviderDeserializer
import io.datawire.loom.terraform.jackson.ResourceBlockDeserializer
import java.nio.file.Path


@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Template(
    @JsonProperty("terraform")
    val terraform: TerraformBlock? = null,

    @JsonProperty("provider")
    @JsonDeserialize(contentUsing = ProviderDeserializer::class)
    val providers: Map<String, Provider> = emptyMap(),

    @JsonProperty("resource")
    val resources: ResourceBlock?,

    @JsonProperty("module")
    @JsonDeserialize(contentUsing = ModuleDeserializer::class)
    val modules: Map<String, Module> = emptyMap(),

    @JsonProperty("output")
    @JsonDeserialize(contentUsing = OutputDeserializer::class)
    val outputs: Map<String, OutputReference> = emptyMap()
) {

  fun render(output: Path) = Json().writeUsingView<TemplateView>(this, output)

  fun render() = Json().writeUsingView<TemplateView>(this)

  fun removeModule(moduleName: String) =
      this.copy(
        modules = modules - moduleName,
        outputs = outputs.filterKeys { it.startsWith("${moduleName}_") }
      )

  fun addModule(module: Module) =
      if (!isModuleNameTaken(module.name)) {
        this.copy(
            modules = modules + (module.name to module),
            outputs = outputs.filterKeys { it.startsWith("${module.name}_") }
        )
      } else {
        throw IllegalArgumentException("Module name '${module.name}' is already in use.")
      }

  private fun isModuleNameTaken(name: String) = name in modules

  private fun isModuleInUseAsDependency(name: String): Boolean {
    for (mod in modules.values) {
      if (mod.isDependent(name)) {
        return true
      }
    }

    return false
  }
}

fun terraformTemplate(
    terraform: TerraformBlock? = null,
    providers: List<Provider> = emptyList(),
    resources: List<Resource> = emptyList(),
    modules: List<Module> = emptyList(),
    outputs: List<OutputReference> = emptyList()
) = Template(
    terraform,
    providers.associateBy { it.name },
    create(resources),
    modules.associateBy { it.name },
    outputs.associateBy { it.name }
)

