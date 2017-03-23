package io.datawire.loom.v2

import io.datawire.loom.v1.exception.LoomException
import io.datawire.loom.v1.fabric.FabricSpec
import io.datawire.loom.v1.kops.Kops
import io.datawire.loom.v2.aws.AwsProvider
import io.datawire.loom.v2.kops.KopsDeleteCluster
import io.datawire.loom.v2.model.DeleteFabric
import io.datawire.loom.v2.model.FabricModel
import io.datawire.loom.v2.model.FabricModelManager
import io.datawire.loom.v2.persistence.S3Persistence
import io.datawire.loom.v2.setup.Bootstrap
import io.datawire.vertx.BaseVerticle
import io.datawire.vertx.fromJson
import io.datawire.vertx.toBuffer
import io.datawire.vertx.toJson
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Future
import io.vertx.core.eventbus.ReplyException
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext


class Loom : BaseVerticle<LoomConfig>(LoomConfig::class) {

    private lateinit var provider: AwsProvider

    override fun start() {
        val router = configureRoutes()

        val server = vertx.createHttpServer().apply {
            requestHandler(router::accept)
            listen(config.api.port, config.api.host)
        }

        logger.info("Loom started! Listening @ http://${config.api.host}:${server.actualPort()}")
    }

    override fun start(startFuture: Future<Void>?) {
        try {
            logger.info("Loom starting...")
            provider = AwsProvider(config.amazon)
            Bootstrap(provider).bootstrap()
            startFuture?.complete()
            start()
        } catch (any: Throwable) {
            logger.error("Loom failed to start!", any)
            startFuture?.fail(any)
            vertx.undeploy(context.deploymentID())
        }
    }

    private fun configureRoutes(): Router {
        val router = Router.router(vertx)

        router.route().failureHandler(this::handleFailure)

        config.authentication.configure(router)

        router.route("/health").apply {
            handler { rc -> rc.response().setStatusCode(200).end("OHai~") }
        }

        router.post("/fabrics").apply {
            consumes("application/json")
            produces("application/json")
            blockingHandler { rc ->
                rc.request().bodyHandler {
                    val spec  = fromJson<FabricSpec>(it)
                    val fm    = FabricModelManager(S3Persistence(provider))
                    val model = fm.getFabricModel(spec.modelId)

                    vertx.eventBus().send<Void>("fabric.create", spec.copy(model = model)) { reply ->
                        when {
                            reply.succeeded() -> rc.response().end(toBuffer(toJson(reply.result().body())))
                            reply.failed()    -> throw reply.cause()
                        }
                    }
                }
            }
        }

        router.get("/fabrics/:name/cluster/config").apply {
            produces("application/vnd.io.kubernetes.kubeconfig+yaml")
            blockingHandler { rc ->

            }
        }

//        router.get("/fabrics/:name").apply {
//            produces("application/json")
//            blockingHandler { rc ->
//                val kops  = Kops(config.kops.copy(stateStore = provider.stateStorageBucket))
//                val fm    = FabricModelManager(S3Persistence(provider))
//                val model = fm.getFabricModel(rc.pathParam("name"))
//                val data = kops.rawClusterInfo("${rc.pathParam("name")}.${model.domain}")
//                rc.response().end(data.encodePrettily())
//            }
//        }
//
//        router.delete("/fabrics/:name/cluster").apply {
//            produces("application/json")
//            blockingHandler { rc ->
//                val kops  = Kops(config.kops.copy(stateStore = provider.stateStorageBucket))
//                val fm    = FabricModelManager(S3Persistence(provider))
//                val model = fm.getFabricModel(rc.pathParam("name"))
//                kops.deleteCluster(DeleteFabric(rc.pathParam("name"), "${rc.pathParam("name")}.${model.domain}"))
//                rc.response().end()
//            }
//        }

        router.post("/fabric-models").apply {
            consumes("application/vnd.FabricModel-v1+json")
            blockingHandler { rc ->
                rc.request().bodyHandler {
                    val model = fromJson<FabricModel>(it)
                    val fm = FabricModelManager(S3Persistence(provider))
                    fm.putFabricModel(model)
                    rc.response().setStatusCode(HttpResponseStatus.CREATED.code()).end()
                }
            }
        }

        router.get("/fabric-models").apply {
            produces("application/json")
            blockingHandler { rc ->
                val fm = FabricModelManager(S3Persistence(provider))
                rc.response().putHeader("Content-Type", "application/json").end(toBuffer(toJson(fm.listModelNames())))
            }
        }

        router.get("/fabric-models/:name").apply {
            produces("application/vnd.FabricModel-v1+json")
            produces("application/json")
            blockingHandler { rc ->
                val fm = FabricModelManager(S3Persistence(provider))
                rc.response()
                        .putHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.FabricModel-v1+json")
                        .end(toBuffer(toJson(fm.getFabricModel(rc.pathParam("name")))))
            }
        }

        return router
    }

    fun handleFailure(rc: RoutingContext) {
        // When the context status is set as 401 then just get out of here. This almost 100% means authentication
        // failure.
        if (rc.statusCode() == 401) {
            rc.next()
            return
        }

        val resp    = rc.response()
        val failure = rc.failure()

        logger.error("Processing requested failed", failure)

        when (failure) {
            is LoomException -> {
                with (resp) {
                    statusCode = failure.httpStatus.code()
                }
            }
            is ReplyException -> {
                with(resp) {
                    statusCode = failure.failureCode()
                }
            }
            else -> {
                with(resp) {
                    statusCode = HttpResponseStatus.INTERNAL_SERVER_ERROR.code()
                }
            }
        }

        resp.end()
    }
}