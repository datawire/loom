//package io.datawire.loom.fabric
//
//import io.datawire.loom.v2.LoomConfig
//import io.datawire.loom.cloud.Provider
//import io.datawire.loom.core.Workspace
//import io.datawire.loom.kops.Kops
//import io.datawire.loom.v1.KopsCreateCluster
//import io.datawire.loom.terraform.Terraform
//import io.datawire.loom.v1.TfConfig
//import io.datawire.loom.terraform.TfProvider
//import io.datawire.vertx.BaseVerticle
//import io.vertx.core.Future
//import io.vertx.core.eventbus.Message
//import java.nio.file.Paths
//
//
//class FabricManager : BaseVerticle<LoomConfig>(LoomConfig::class) {
//
//    private val workspace: Workspace by lazy { Workspace(vertx) }
//
//    override fun start(startFuture: Future<Void>?) {
//        registerEventBusCodec<Fabric>()
//        registerEventBusCodec<FabricModel>()
//
//        super.start(startFuture)
//    }
//
//    override fun start() {
//        vertx.eventBus().localConsumer<FabricSpec>("fabric.create").handler(this::createFabricV2)
//    }
//
//    fun createFabricV2(msg: Message<FabricSpec>) {
//        val spec = msg.body()
//        logger.info("Fabric creation started (name: {})", spec.name)
//
//
//    }
//
//    fun createFabric(msg: Message<Fabric>) {
//        val fabric = msg.body()
//        logger.info("Create fabric ${fabric.name}")
//
//        val workspace = Workspace(vertx)
//        if (!workspace.directoryExists("fabrics/${fabric.name}")) {
//            workspace.createDirectoryBlocking("fabrics/${fabric.name}")
//        }
//
//        try {
//            msg.reply(fabric.copy(status = FabricStatus.IN_PROGRESS))
//            val model = getFabricModelById(fabric.model)
//            val tf = Terraform(vertx, config.terraform, Paths.get("fabrics/${fabric.name}"))
//
//            val config = TfConfig("s3", mapOf(
//                    "bucket"     to "datawire-loom",
//                    "key"        to "${fabric.name}.tfstate",
//                    "encrypt"    to "true",
//                    "region"     to "us-east-1",
//                    "acl"        to "private",
//                    "lock_table" to "terraform_state"
//            ))
//            val provider = TfProvider("aws", mapOf("region" to "us-east-1"))
//
//            val networkingModule = tf.generateNetworkingModule(model.networking.module, mapOf(
//                    "cidr_block"       to "10.0.0.0/16",
//                    "name"             to fabric.name
//            ))
//
//            val tfTemplate = tf.generateTemplate(config, provider, listOf(networkingModule))
//            workspace.writeJsonFileBlocking("fabrics/${fabric.name}/main.tf.json", tfTemplate)
//
//            tf.init()
//
//            if (tf.plan()) {
//                tf.apply()
//            }
//
//            val output = tf.output()
//            logger.debug("Terraform Output\n\n{}", output)
//
//            val vpcId = output.getJsonObject("main_vpc_id").getString("value")
//            val vpcCidr = output.getJsonObject("main_vpc_cidr").getString("value")
//            val vpcAz = output.getJsonObject("main_vpc_availability_zones").getJsonArray("value").list?.map { it.toString() }!!
//
//            val createCluster = KopsCreateCluster(vpcAz, vpcId, vpcCidr, "${fabric.name}.${model.domain}")
//
//            val kops = Kops(this.config.kops.copy(stateStore = "s3://${config.params["bucket"]}"))
//            kops.createCluster(createCluster)
//
//        } catch (any: Throwable) {
//            msg.fail(0, "No such fabric model: ${fabric.model}")
//        }
//    }
//
//    fun getFabric(msg: Message<String>) {
//
//    }
//
//    private fun getFabricModelById(id: String): FabricModel {
//        return workspace.readJsonFileBlocking<FabricModel>("fabric-models/$id.json")
//    }
//
//    private fun isRegisteredModel(name: String): Boolean {
//        return try {
//            getFabricModelById("id")
//            true
//        } catch (any: Throwable) {
//            false
//        }
//    }
//
//    private fun isNameAvailable(name: String): String {
//        return ""
//    }
//
//    private fun lookupCredentials(provider: Provider) {
//
//    }
//}