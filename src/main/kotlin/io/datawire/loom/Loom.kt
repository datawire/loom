package io.datawire.loom

import io.datawire.loom.aws.AwsProvider
import io.datawire.loom.config.LoomConfig
import io.datawire.loom.core.Bootstrap
import io.datawire.loom.data.*
import io.datawire.loom.exception.*
import io.datawire.loom.fabric.FabricManager
import io.datawire.loom.internal.exception
import io.datawire.loom.model.*
import org.eclipse.jetty.http.HttpStatus
import org.slf4j.LoggerFactory
import spark.Request
import spark.Response
import spark.Route
import spark.Spark.*
import java.time.Instant

class Loom(val config: LoomConfig) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val awsProvider = AwsProvider(config.amazon)

    private val modelsDao      = AwsS3Dao<FabricModel>(awsProvider, FabricModel::class, "models")
    private val fabricsDao     = AwsS3Dao<Fabric>(awsProvider, Fabric::class, "fabrics")

    private val fabricManager = FabricManager(
            config.terraform, config.kops, awsProvider, modelsDao, fabricsDao)

    fun run() {
        Bootstrap(awsProvider).bootstrap()

        port(config.port)
        ipAddress(config.host)

        exceptionHandlers()

        exception(NotFoundException::class.java) { ex, req, res ->
            (ex as? NotFoundException)?.let {
                res.status(404)
                res.body(toJson(it.notFound))
            }
        }

        exception(ExistsAlreadyException::class.java) { ex, req, res ->
            (ex as? ExistsAlreadyException)?.let {
                res.status(409)
                res.body(toJson(it.existsAlready))
            }
        }

        get("/health") { req, res -> "I am healthy" }

        modelApi()
        fabricApi()

        logger.info("== Loom has started ...")
        logger.info(">> Listening on ${config.host}:${port()}")
    }

    private fun modelApi() {
        get("/models/:id", "application/json", Route(
                { req, res ->
                    val id = req.params(":id")
                    res.header("Content-Type", "application/json")
                    modelsDao.get(req.params(":id")) ?: throw NotFoundException(ModelNotFound(id))
                }), Jsonifier())

        delete("/models/:id") { req, res ->
            val id = req.params(":id")
            modelsDao.delete(id)

            res.status(204)
            ""
        }

        post("/models") { req, res ->
            val model = fromJson<FabricModel>(req.body())
            modelsDao.put(model.id, model.copy(creationTime = Instant.now()))
            res.status(204)
        }
    }

    private fun fabricApi() {
        post("/fabrics") { req, res ->
            val fabric = fromJson<Fabric>(req.body())
            fabricManager.create(fabric)

            res.status(204)
        }

        get("/fabrics/:name", "application/json", Route(
                { req, res ->
                    val id = req.params(":name")
                    fabricsDao.get(req.params(":name")) ?: throw NotFoundException(FabricNotFound(id))
                }), Jsonifier())

        get("/fabrics/:name/cluster/config") { req, res ->
            res.header("Content-Type", "application/yaml")
            fabricManager.getClusterConfig(req.params(":name"))
        }

        delete("/fabrics/:name") { req, res ->
            val fabricName = req.params(":name")
            fabricsDao.delete(fabricName)
            res.status(204)
            ""
        }

        delete("/fabrics/:name/cluster") { req, res ->
            val fabricName = req.params(":name")
            val fabric = fabricsDao.get(fabricName) ?: throw NotFoundException(FabricNotFound(fabricName))
            fabricManager.deleteCluster(fabric)

            res.status(204)
            ""
        }
    }

    private fun exceptionHandlers() {
        fun handleError(ex: Exception, req: Request, res: Response) {
            val loomEx = (ex as? LoomException) ?: LoomException(cause = ex)

            logger.error("Exception processing request ({}, {})", req.requestMethod(), req.pathInfo(), loomEx)

            val errorInfo = lookupByException(ex)
            val errorDetail = ErrorDetail(
                    errorInfo.id.toString(),
                    errorInfo.name,
                    errorInfo.description)

            with(res) {
                res.status(errorInfo.httpStatusCode)
                res.header("Content-Type", "application/json")
                res.body(toJson(Results("errors", listOf(errorDetail))))
            }
        }

        exception<ResourceNotExistsException>(::handleError)
        exception<ResourceExistsException>(::handleError)
    }

    private fun toError() {

    }
}