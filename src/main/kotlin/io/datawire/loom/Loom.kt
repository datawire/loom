package io.datawire.loom

import io.datawire.loom.api.FABRIC_MODEL_TYPE
import io.datawire.loom.core.Workspace
import io.datawire.loom.fabric.Fabric
import io.datawire.loom.fabric.FabricManager
import io.datawire.loom.fabric.FabricModel
import io.datawire.vertx.BaseVerticle
import io.datawire.vertx.fromJson
import io.datawire.vertx.toBuffer
import io.datawire.vertx.toJson
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import java.nio.file.Paths


class Loom : BaseVerticle<LoomConfig>(LoomConfig::class) {

    override fun start(startFuture: Future<Void>?) {
        val cm = vertx.sharedData().getLocalMap<String, String>("loom.config")
        cm.putIfAbsent("workspace", toJson(Workspace.Config(path = Paths.get("workspace"))))

        vertx.deployVerticle(FabricManager::class.qualifiedName, DeploymentOptions().setConfig(config()).setWorker(true))

        val router = configureRouter()
        vertx.createHttpServer().apply {
            requestHandler(router::accept)
            listen(config.api.port)
        }

        super.start(startFuture)
    }

    private fun configureRouter(): Router {
        val router = Router.router(vertx)

        router.route().handler(BodyHandler.create())
        router.route("/*").handler {
            logger.debug("HTTP Method = {}", it.request().method())
            logger.debug("HTTP Path   = {}", it.request().path())
            logger.debug("HTTP Headers = {}", it.request().headers())
            it.next()
        }

        router.get("/health").handler { it.response().setStatusCode(200).end() }

        router.post("/profiles/:name").apply {

        }

        router.get("/profiles").apply {

        }

        router.get("/fabrics/:name").apply {

        }

        router.get("/fabrics/:name/status").apply {

        }

        router.get("/fabrics").apply {

        }

        router.delete("/fabrics/:name").apply {

        }

        router.post("/fabric-models").apply {
            consumes("application/json")
            produces("application/json")
            handler { rc ->
                val fabricModel = fromJson<FabricModel>(rc.bodyAsJson)
                vertx.eventBus().send<FabricModel>("fabric-model.create", fabricModel) { reply ->
                    when {
                        reply.succeeded() -> rc.response().end(toBuffer(toJson(reply.result().body())))
                        reply.failed()    -> throw reply.cause()
                    }
                }
            }
        }

        router.post("/fabrics").apply {
            consumes("application/json")
            produces("application/json")
            handler { rc ->
                val fabric = fromJson<Fabric>(rc.bodyAsJson)
                vertx.eventBus().send<Fabric>("fabric.create", fabric) { reply ->
                    when {
                        reply.succeeded() -> rc.response().end(toBuffer(toJson(reply.result().body())))
                        reply.failed()    -> throw reply.cause()
                    }
                }
            }
        }

        return router
    }
}
