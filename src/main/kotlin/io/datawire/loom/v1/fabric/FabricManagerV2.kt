package io.datawire.loom.v1.fabric

import io.datawire.loom.v2.LoomConfig
import io.datawire.loom.v1.core.Workspace
import io.datawire.loom.v1.exception.AlreadyExistsException
import io.datawire.loom.v1.exception.NotFoundException
import io.datawire.loom.v1.kops.Kops
import io.datawire.loom.v1.kops.KopsCreateCluster
import io.datawire.loom.v1.terraform.Terraform
import io.datawire.loom.v1.terraform.TfConfig
import io.datawire.loom.v1.terraform.TfProvider
import io.datawire.vertx.BaseVerticle
import io.vertx.core.Future
import io.vertx.core.eventbus.Message
import io.vertx.core.file.FileSystemException
import java.nio.file.Paths
import java.time.Instant


class FabricManagerV2 : BaseVerticle<LoomConfig>(LoomConfig::class) {

    private val workspace: Workspace by lazy { Workspace(vertx) }

    private val fabricsRoot = "fabrics/"
    private fun fabricRoot(name: String) = "$fabricsRoot/$name"

    override fun start(startFuture: Future<Void>?) {
        registerEventBusCodec<Fabric>()
        registerEventBusCodec<FabricModel>()

        super.start(startFuture)
    }

    override fun start() {
        vertx.eventBus().localConsumer<FabricSpec>("fabric.create").handler(this::createFabric)
        vertx.eventBus().localConsumer<FabricDeploymentContext>("fabric.create-networking").handler(this::createNetworking)
        vertx.eventBus().localConsumer<FabricDeploymentContext>("fabric.create-cluster").handler(this::createCluster)
        vertx.eventBus().localConsumer<String>("fabric.status").handler(this::queryStatus)
    }

    fun exists(id: String) = workspace.directoryExists("$fabricsRoot/$id")


    private fun get(name: String): Fabric {
        return try {
            workspace.readJsonFileBlocking("$fabricsRoot/$name.json")
        } catch (fse: FileSystemException) {
            if (fse.cause is java.nio.file.NoSuchFileException) {
                throw NotFoundException()
            } else {
                throw fse
            }
        }
    }

    private fun delete(name: String) {
        try {
            workspace.delete("$fabricsRoot/$name.json")
        } catch (fse: FileSystemException) {
            if (fse.cause is NoSuchFileException) { /* File is deleted already */ }
        }
    }

    private fun createFabricRoot(name: String) {
        if (!exists(name)) {
            workspace.createDirectoryBlocking(fabricRoot(name))
        } else {
            throw AlreadyExistsException()
        }
    }

    private fun putJson(fabricName: String, name: String, data: Any) {
        if (!exists(fabricName)) {
            workspace.writeJsonFileBlocking("${fabricRoot(fabricName)}/$name", data)
        } else {
            throw AlreadyExistsException()
        }
    }

    fun list(): List<Fabric> {
        return workspace.listDirectories(fabricsRoot).map { workspace.readJsonFileBlocking<Fabric>("$it/fabric.json") }
    }

    private fun queryStatus(msg: Message<String>) {
        try {
            msg.reply(FabricDeploymentContext.get(vertx, msg.body()))
        } catch (any: Throwable) {
            msg.fail(404, any.message)
        }
    }

    private fun createCluster(msg: Message<FabricDeploymentContext>) {
//        var ctx = msg.body()
//        try {
//            ctx = ctx.copy(status = FabricStatus.KUBERNETES_CREATION_STARTED)
//            ctx.updateStatus(vertx)
//
//            val spec  = ctx.specification
//            val model = spec.model!!
//
//            val createCluster = KopsCreateCluster(spec.zones, spec.vpcId!!, spec.vpcCidr!!, "${spec.name}.${model.domain}")
//
//            val kops = Kops(this.config.kops.copy(stateStore = "s3://datawire-loom"))
//            kops.createCluster(createCluster)
//
//            ctx = ctx.copy(status = FabricStatus.NETWORKING_CREATION_COMPLETED)
//            ctx.updateStatus(vertx)
//        } catch (any: Throwable) {
//            ctx.copy(status = FabricStatus.FAILED).updateStatus(vertx)
//        }
    }

    private fun createNetworking(msg: Message<FabricDeploymentContext>) {
//        var ctx = msg.body()
//        try {
//            ctx = ctx.copy(status = FabricStatus.NETWORKING_CREATION_STARTED)
//            ctx.updateStatus(vertx)
//
//            val spec  = ctx.specification
//            val model = spec.model!!
//
//            val tf = Terraform(config.terraform, Paths.get(fabricRoot(spec.name)))
//            val tfConfig = TfConfig("s3", mapOf(
//                    "bucket"     to "datawire-loom",
//                    "key"        to "${spec.name}/terraform.tfstate",
//                    "encrypt"    to "true",
//                    "region"     to "us-east-1",
//                    "acl"        to "private",
//                    "lock_table" to "terraform_state"
//            ))
//
//            val provider = TfProvider("aws", mapOf("region" to "us-east-1"))
//            val networking = tf.generateNetworkingModule(model.networking.module, mapOf(
//                    "cidr_block"       to "10.0.0.0/16",
//                    "name"             to spec.name
//            ))
//
//            val tfTemplate = tf.generateTemplate(tfConfig, provider, listOf(networking))
//            putJson(spec.name, "main.tf.json", tfTemplate)
//
//            tf.init()
//
//            if (tf.plan()) {
//                tf.apply()
//            }
//
//            val tfOutput = tf.output()
//
//            val vpcId   = tfOutput.getJsonObject("main_vpc_id").getString("value")
//            val vpcCidr = tfOutput.getJsonObject("main_vpc_cidr").getString("value")
//            val vpcAz   = tfOutput.getJsonObject("main_vpc_availability_zones").getJsonArray("value").list?.map(Any?::toString)!!
//
//            val specWithNet = spec.copy(vpcId = vpcId, vpcCidr = vpcCidr, zones = vpcAz)
//
//            ctx = ctx.copy(
//                    specification = specWithNet,
//                    status = FabricStatus.NETWORKING_CREATION_COMPLETED)
//
//            ctx.updateStatus(vertx)
//
//            vertx.eventBus().send("fabric.create-cluster", ctx)
//        } catch (any: Throwable) {
//            ctx.copy(status = FabricStatus.FAILED).updateStatus(vertx)
//        }
    }

    fun createFabric(msg: Message<FabricSpec>) {
        val spec = msg.body()
        if (exists(spec.name)) {
            msg.fail(409, "Fabric exists already (name: ${spec.name})")
        }

        try {
            logger.info("Fabric creation started (name: {})", spec.name)
            createFabricRoot(spec.name)
            val ctx = FabricDeploymentContext(spec.name, spec, FabricStatus.NOT_STARTED, Instant.now())
            vertx.eventBus().send("fabric.create-networking", ctx)
        } catch (any: Throwable) {
            logger.error("Fabric creation failed (name: {})", spec.name, any)
            msg.fail(500, "Fabric creation failed (name: ${spec.name})")
        }
    }
}