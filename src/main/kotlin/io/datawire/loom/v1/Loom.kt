package io.datawire.loom.v1

import io.datawire.loom.v1.core.Workspace
import io.datawire.loom.v1.exception.LoomException
import io.datawire.loom.v1.kops.Kops
import io.datawire.loom.v1.fabric.FabricManagerV2
import io.datawire.loom.v1.fabric.FabricModel
import io.datawire.loom.v1.fabric.FabricSpec
import io.datawire.loom.v1.fabric.FileFabricModelStore
import io.datawire.loom.v2.LoomConfig
import io.datawire.vertx.BaseVerticle
import io.datawire.vertx.fromJson
import io.datawire.vertx.toBuffer
import io.datawire.vertx.toJson
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.eventbus.ReplyException
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import java.nio.file.Paths


class Loom : BaseVerticle<LoomConfig>(LoomConfig::class) {

    private val workspace        by lazy { Workspace(vertx) }
    private val fabricModelStore by lazy { FileFabricModelStore(workspace) }

    override fun start(startFuture: Future<Void>?) {
        val cm = vertx.sharedData().getLocalMap<String, String>("loom.config")
        cm.putIfAbsent("workspace", toJson(Workspace.Config(path = Paths.get("workspace"))))

        vertx.deployVerticle(FabricManagerV2::class.qualifiedName, DeploymentOptions().setConfig(context.config()).setWorker(true))

        val router = configureRouter()
        vertx.createHttpServer().apply {
            requestHandler(router::accept)
            listen(config.api.port)
        }

        super.start(startFuture)
    }

    private fun configureRouter(): Router {
        val router = Router.router(vertx)

        router.route().apply {
            handler(BodyHandler.create())
            failureHandler(this@Loom::handleFailure)
        }

        router.route("/*").handler {
            logger.debug("HTTP Method = {}", it.request().method())
            logger.debug("HTTP Path   = {}", it.request().path())
            logger.debug("HTTP Headers = {}", it.request().headers())
            it.next()
        }

        router.get("/health").handler { it.response().setStatusCode(200).end() }

        // -------------------------------------------------------------------------------------------------------------
        // Fabrics API
        // -------------------------------------------------------------------------------------------------------------

//        router.post("/fabrics").apply {
//            consumes("application/json")
//            produces("application/json")
//            handler { rc ->
//                val spec  = fromJson<FabricSpec>(rc.bodyAsJson)
//                val model = fabricModelStore.get(spec.modelId)
//
//                vertx.eventBus().send<Void>("fabric.create", spec.copy(model = model)) { reply ->
//                    when {
//                        reply.succeeded() -> rc.response().end(toBuffer(toJson(reply.result().body())))
//                        reply.failed()    -> throw reply.cause()
//                    }
//                }
//            }
//        }

        router.get("/fabrics").apply {
//            produces("application/json")
//            blockingHandler { rc ->
//                val fabrics = fabricStore.list()
//                rc.response().putHeader("Content-Type", "application/json").end(toBuffer(toJson(fabrics)))
//            }
        }

        router.get("/fabrics/:name").apply {
            produces("application/json")
            blockingHandler { rc ->

                val kops = Kops(config.kops)
            }

//            handler { rc ->
//                vertx.eventBus().send<String>("fabric.get", rc.pathParam("name")) { reply ->
//                    when {
//                        reply.succeeded() -> rc.response().end(toBuffer(toJson(reply.result().body())))
//                        reply.failed()    -> throw reply.cause()
//                    }
//                }
//            }
        }

        router.get("/fabrics/:name/status").apply {
            produces("application/json")
            handler { rc ->
                vertx.eventBus().send<String>("fabric.status", rc.pathParam("name")) { reply ->
                    when {
                        reply.succeeded() -> rc.response().end(toBuffer(toJson(reply.result().body())))
                        reply.failed()    -> throw reply.cause()
                    }
                }
            }
        }

        router.delete("/fabrics/:name").apply {
            blockingHandler {

            }
        }

        // -------------------------------------------------------------------------------------------------------------
        // Fabric Models API
        // -------------------------------------------------------------------------------------------------------------

        router.post("/fabric-models").apply {
            consumes("application/json")
            blockingHandler { rc ->
                val model = fromJson<FabricModel>(rc.bodyAsJson)
                fabricModelStore.put(model)
                rc.response().setStatusCode(HttpResponseStatus.CREATED.code()).end()
            }
        }

        router.get("/fabric-models").apply {
            produces("application/json")
            blockingHandler { rc ->
                val models = fabricModelStore.list()
                rc.response().putHeader("Content-Type", "application/json").end(toBuffer(toJson(models)))
            }
        }

        router.get("/fabric-models/:name").apply {
            produces("application/json")
            blockingHandler { rc ->
                val model = fabricModelStore.get(rc.pathParam("name"))
                rc.response().end(toBuffer(toJson(model)))
            }
        }

        router.delete("/fabric-models/:name").apply {
            blockingHandler { rc ->
                fabricModelStore.delete(rc.pathParam("name"))
                rc.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end()
            }
        }

        return router
    }

    fun handleFailure(rc: RoutingContext) {
        // When the context status is set as 401 then just get out of here. This almost 100% means a JWT failed to
        // decode or validate.
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
