package io.datawire.loom

import io.datawire.loom.proto.aws.AwsProvider
import io.datawire.loom.proto.config.LoomConfig
import io.datawire.loom.proto.core.Bootstrap
import io.datawire.loom.proto.data.AwsS3Dao
import io.datawire.loom.proto.data.Jsonifier
import io.datawire.loom.proto.data.fromJson
import io.datawire.loom.proto.data.toJson
import io.datawire.loom.proto.exception.LoomException
import io.datawire.loom.proto.exception.fabricNotExists
import io.datawire.loom.proto.exception.modelNotExists
import io.datawire.loom.proto.fabric.FabricManager
import io.datawire.loom.proto.internal.exception
import io.datawire.loom.proto.model.Fabric
import io.datawire.loom.proto.model.FabricModel
import io.datawire.loom.v1.FabricController
import io.datawire.loom.v1.KopsKubernetesClusterService
import io.datawire.loom.v1.auth.SingleUserAuthenticator
import io.datawire.loom.v1.kops.Kops
import org.slf4j.LoggerFactory
import spark.Request
import spark.Response
import spark.Route
import spark.Spark.*
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant

class Loom(val config: LoomConfig) {

  private val logger = LoggerFactory.getLogger(javaClass)

  private val awsProvider = AwsProvider(config.amazon)

  private val modelsDao = AwsS3Dao<FabricModel>(awsProvider, FabricModel::class, "models")
  private val fabricsDao = AwsS3Dao<Fabric>(awsProvider, Fabric::class, "fabrics")

  private val fabricManager = FabricManager(
      config.terraform, config.kops, awsProvider, modelsDao, fabricsDao)

  fun run() {
    Bootstrap(awsProvider).bootstrap()

    port(config.port)
    ipAddress(config.host)

    if (System.getProperty("loom.auth.required", "false") == "true") {
      before("/*", SingleUserAuthenticator("admin", System.getProperty("loom.auth.admin-password", "admin")))
    }

    exceptionHandlers()

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
          modelsDao.get(req.params(":id")) ?: throw modelNotExists(id)
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

    get("/models", "application/json", Route(
        { _, _ ->
          modelsDao.listItems("models/")
        }), Jsonifier())
  }

  private fun fabricApi() {
    val kopsExec = config.kops.executable
    val fabricController = FabricController(
        fabricsDao,
        KopsKubernetesClusterService(
            kops = { cluster ->
              val fabric = cluster.substring(0..cluster.indexOf(".") - 1)
              Kops(kopsExec,
                  awsProvider.stateStorageBucketName,
                  Files.createDirectories(Paths.get("/tmp/fabric-$fabric")
                  )
              )
            }
        )
    )

    post("/fabrics") { req, res ->
      val fabric = fromJson<Fabric>(req.body())
      fabricManager.create(fabric)

      res.status(204)
    }

    get("/fabrics", "application/json", Route(
        { _, _ ->
          fabricsDao.listItems("fabrics/")
        }), Jsonifier())

    get("/fabrics/:name", "application/json", Route(
        { req, _ ->
          val id = req.params(":name")
          fabricsDao.get(req.params(":name")) ?: throw fabricNotExists(id)
        }), Jsonifier())

    get("/fabrics/:name/cluster", "application/json", Route(
        { req, res ->
          res.header("Content-Type", "application/json")
          fabricController.getCluster(req.params(":name"))
        }), Jsonifier())

    get("/fabrics/:name/cluster/config") { req, res ->
      res.header("Content-Type", "application/yaml")
      fabricController.getClusterContext(req.params(":name"))
    }

    delete("/fabrics/:name") { req, res ->
      val fabricName = req.params(":name")
      fabricsDao.delete(fabricName)
      res.status(204)
      ""
    }

    delete("/fabrics/:name/cluster") { req, res ->
      val fabricName = req.params(":name")
      val fabric = fabricsDao.get(fabricName) ?: throw fabricNotExists(fabricName)
      fabricManager.deleteCluster(fabric)

      res.status(204)
      ""
    }
  }

  private fun exceptionHandlers() {
    fun buildResponse(ex: Exception, req: Request, res: Response) {
      val temp = ex as? LoomException ?: LoomException(cause = ex)
      val (httpStatus, errors) = temp.getStatusCodeAndErrorDetails()
      res.status(httpStatus)
      res.header("Content-Type", "application/json")
      res.body(toJson(errors))
    }

    notFound { req, res ->
      res.status(404)
      res.body()
    }

    exception<LoomException>(::buildResponse)
    exception<Exception>(::buildResponse)
  }
}