package io.datawire.loom.dev


import io.datawire.loom.dev.aws.AwsBootstrap
import io.datawire.loom.dev.aws.createAwsCloud
import io.datawire.loom.dev.core.*
import io.datawire.loom.dev.core.kubernetes.KopsKubernetesClusterManager
import io.datawire.loom.dev.model.validation.ValidationException
import io.datawire.loom.proto.config.LoomConfig
import io.datawire.loom.proto.data.JSON_MAPPER
import io.datawire.loom.proto.data.Jsonifier
import io.datawire.loom.proto.data.fromYaml
import io.datawire.loom.proto.data.toJson
import io.datawire.loom.proto.exception.LoomException
import org.slf4j.LoggerFactory
import spark.Route
import spark.Spark.*
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

typealias HostAndPort = Pair<String, Int>

class Loom(
    private val port: Int,
    private val host: String = "",
    private val fabricModelController: FabricModelController,
    private val fabricController: FabricController
) {

  private val log = LoggerFactory.getLogger(javaClass)

  private val acceptJson = "application/json"
  private val jsonifier = Jsonifier()

  fun run() {
    port(port)
    ipAddress(host)

    initExceptionHandlers()
    initApi()

    log.info("== Loom has started ...")
    log.info(">> Listening on $host:${port()}")
  }

  private fun initApi() {
    get("/health") { _, _ -> "" }
    log.info("== Registered health check endpoint (/health)")

    path("/api") {
      delete ("/fabrics",     acceptJson, Route(fabricController::deleteFabric))
      get    ("/fabrics",     acceptJson, Route { _, _ -> }, jsonifier)
      get    ("/fabrics/:id", acceptJson, Route(fabricController::fetchFabric), jsonifier)
      post   ("/fabrics",     acceptJson, Route(fabricController::createFabric))

      delete ("/models",      acceptJson, Route(fabricModelController::deleteModel))
      get    ("/models/:id",  acceptJson, Route(fabricModelController::fetchModel), jsonifier)
      post   ("/models",      acceptJson, Route(fabricModelController::createModel))
    }
    log.info("== Registered API endpoints (/api*)")
  }

  private fun initExceptionHandlers() {
    notFound { _, res ->
      res.status(404)
      res.body()
    }

    exception(ValidationException::class.java) { ex, _, res ->
      log.error("Error validating incoming data", ex)

      res.apply {
        status(422)
        header("Content-Type", "application/json")
        body(toJson((ex as ValidationException).issues))
      }
    }

    exception(LoomException::class.java) { ex, _, res ->
      log.error("Error handling request", ex)

      val loomException = (ex as? LoomException) ?: LoomException(cause = ex)
      val (httpStatus, errors) = loomException.getStatusCodeAndErrorDetails()

      res.apply {
        status(httpStatus)
        header("Content-Type", "application/json")
        body(toJson(errors))
      }
    }
  }
}

fun main(args: Array<String>) {
  configureProperties()

  val configFile = if (args.isNotEmpty()) Paths.get(args[0]) else Paths.get("config/loom.json")
  val config = fromYaml<LoomConfig>(configFile)

  initLoomWorkspace()
  bootstrapLoom(config).run()
}

fun bootstrapLoom(config: LoomConfig): Loom {
  val aws = createAwsCloud(config.amazon)

  AwsBootstrap(aws).bootstrap()

  val fabricModelDao = InMemoryFabricModelDao()
  val fabricModelController = FabricModelController(
      JSON_MAPPER,
      FabricModelValidator(aws),
      fabricModelDao
  )

  val fabricDao = InMemoryFabricDao()
  val fabricController = FabricController(
      JSON_MAPPER,
      FabricValidator(fabricModelDao),
      fabricModelDao,
      fabricDao,
      KopsKubernetesClusterManager()
  )

  val loom = Loom(
      host = config.host,
      port = config.port,
      fabricController = fabricController,
      fabricModelController = fabricModelController
  )

  return loom
}

private fun initLoomWorkspace() {
  Files.createDirectories(Paths.get(System.getProperty("user.home"), "loom"))
}

private fun configureProperties() {
  val props = Properties()
  props.load(FileInputStream("config/server.properties"))
  for ((name, value) in props) {
    System.setProperty(name.toString(), value.toString())
  }
}
