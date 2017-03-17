package io.datawire.loom.terraform

import io.datawire.loom.config.ExternalProgramConfig
import io.datawire.loom.core.Workspace
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.kotlin.core.json.JsonObject
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.ProcessResult
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.nio.file.Path
import java.nio.file.Paths


class Terraform(private val vertx: Vertx, private val config: ExternalProgramConfig, private val workspace: Path) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val defaultOptions = setOf("-no-color", "-input=false")

    val terraform get() = config.executable.toString()
    val version: String by lazy { terraform("version").outputUTF8().lines()[0].replace("Terraform v", "") }

    data class BackendConfig(val bucket: String, val key: String, val encrypt: Boolean = true, val acl: String = "private")

    fun generateTemplate(
            config: TfConfig, provider: TfProvider, modulesAndOutputs: List<Pair<TfModule, List<TfOutput>>>): TfTemplate {

        val template = TfTemplate(
                config    = mapOf(config.type to config),
                providers = mapOf(provider.name to provider))

        return modulesAndOutputs.fold(template) { template, (module, outputs) ->
            template.copy(
                    modules = template.modules + Pair(module.name, module),
                    outputs = template.outputs + outputs.associateBy { it.name }) }
    }

    fun generateNetworkingModule(source: String, params: Map<String, Any>): Pair<TfModule, List<TfOutput>> {
        val module  = TfModule("main", source, params)
        val outputs = listOf(
                TfOutput("main_vpc_id", "\${module.main.id}"),
                TfOutput("main_vpc_cidr", "\${module.main.cidr_block}"),
                TfOutput("main_vpc_availability_zones", "\${module.main.availability_zones}")
        )

        return Pair(module, outputs)
    }

    fun init() {
        val res = terraform("init", "-no-color", "-input=false", "-backend=true", "-get")
        terraform("update", "-get")
    }

    fun plan(): Boolean {
        val res = terraform("plan", "-no-color", "-detailed-exitcode", "-input=false", "-out=plan.out")
        return res.exitValue == 2
    }

    fun apply() {
        val res = terraform("apply", "-no-color", "-input=false", "plan.out")
    }

    fun output(): JsonObject {
        val res = terraform("output", "-no-color", "-json")
        if (res.exitValue == 0) {
            return JsonObject(res.outputUTF8())
        } else {
            throw IllegalStateException("No output for fabric... $workspace")
        }
    }

    fun get() {
        val res = terraform("get", "-no-color", "-input=false", "-update=true")
    }

    private fun terraform(vararg args: String): ProcessResult {
        val pe = ProcessExecutor().apply {
            command(listOf(terraform) + args)
            redirectOutput(Slf4jStream.ofCaller().asInfo())
            directory(Paths.get("workspace").resolve(workspace).toFile())
            readOutput(true)
        }

        return pe.execute()
    }
}