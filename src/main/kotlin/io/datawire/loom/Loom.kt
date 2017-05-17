package io.datawire.loom

import io.datawire.loom.core.Json
import io.datawire.loom.core.Jsonifier
import io.datawire.loom.core.LoomException
import io.datawire.loom.core.aws.AwsBootstrap
import io.datawire.loom.core.aws.AwsCloud
import io.datawire.loom.core.aws.createAwsCloud
import io.datawire.loom.core.validation.ValidationException
import io.datawire.loom.core.validation.Validator
import io.datawire.loom.fabric.*
import io.datawire.loom.fabric.FabricModelValidator
import io.datawire.loom.fabric.FabricParametersValidator
import io.datawire.loom.fabric.InMemoryFabricModelDao
import org.slf4j.LoggerFactory
import spark.Request
import spark.Response
import spark.Route
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import spark.Spark.*

class Loom(
    private val port: Int,
    private val host: String = "",
    private val aws: AwsCloud,
    private val fabricModelDao: FabricModelDao,
    private val fabricSpecDao: FabricSpecDao,
    private val fabricService: FabricService,
    private val json: Json
) {

  private val log = LoggerFactory.getLogger(Loom::class.java)

  private val acceptJson = "application/json"
  private val jsonifier  = Jsonifier(json)

  private val fabricModelValidator  = FabricModelValidator(aws)
  private val fabricParamsValidator = FabricParametersValidator(fabricSpecDao, fabricModelDao)

  fun run() {
    port(port)
    ipAddress(host)

    initExceptionHandlers()
    initApi()

    log.info("== Loom has started ...")
    log.info(">> Listening on $host:${port()}")
  }

  // -------------------------------------------------------------------------------------------------------------------
  // Fabrics
  // -------------------------------------------------------------------------------------------------------------------

  private fun removeFabric(req: Request, res: Response) { }

  private fun addFabric(req: Request, res: Response): FabricSpec {
    val config = fromJson<FabricConfig>(req.body(), fabricParamsValidator)

    val spec = fabricService.createFabric(config)
    res.header("Content-Type", "application/json")
    return spec
  }

  private fun fetchFabric(req: Request, res: Response) {
    TODO()
  }

  private fun addResourceToFabric(req: Request, res: Response) {
    TODO()
  }

  private fun removeResourceFromFabric(req: Request, res: Response) {

  }

  // -------------------------------------------------------------------------------------------------------------------
  // Fabric Models
  // -------------------------------------------------------------------------------------------------------------------

  private fun decommissionModel(req: Request, @Suppress("UNUSED_PARAMETER") res: Response): FabricModel? {
    return fabricService.decommissionModel(req.params(":name"))?.apply {
      res.header("Content-Type", "application/json")
    }
  }

  private fun fetchModel(req: Request, res: Response): FabricModel? {
    return fabricService.fetchModel(req.params(":name"))?.apply {
      res.header("Content-Type", "application/json")
    }
  }

  private fun createModel(req: Request, res: Response): FabricModel {
    val model = fromJson<FabricModel>(req.body(), fabricModelValidator)
    return fabricService.createModel(model)
  }

  // -------------------------------------------------------------------------------------------------------------------
  // HTTP API
  // -------------------------------------------------------------------------------------------------------------------

  private fun initApi() {
    get("/health") { _, _ -> "" }
    log.info("== Registered health check endpoint (/health)")

    path("/api") {
      delete ("/fabrics",     acceptJson, Route(this::removeFabric))
      get    ("/fabrics/:id", acceptJson, Route(this::fetchFabric), jsonifier)
      post   ("/fabrics",     acceptJson, Route(this::addFabric), jsonifier)

      post   ("/fabrics/:name/resources", acceptJson, Route(this::addResourceToFabric))
      delete ("/fabrics/:name/resources/:name", acceptJson, Route(this::removeResourceFromFabric))

      delete ("/models/:name", acceptJson, Route(this::decommissionModel), jsonifier)
      get    ("/models/:name", acceptJson, Route(this::fetchModel), jsonifier)
      post   ("/models",       acceptJson, Route(this::createModel), jsonifier)
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
        body(json.write((ex as ValidationException).issues))
      }
    }

    exception(LoomException::class.java) { ex, _, res ->
      log.error("Error handling request", ex)

      //val (httpStatus, errors) = loomException.getStatusCodeAndErrorDetails()

      res.apply {
        status(ex.statusCode)
        //header("Content-Type", "application/json")
        //body(toJson(errors))
      }
    }
  }

  private inline fun <reified T: Any> fromJson(text: String, validator : Validator? = null): T {
    val jsonNode = json.toJsonNode(text)
    validator?.apply { validate(jsonNode) }
    return json.read(jsonNode)
  }
}

fun main(args: Array<String>) {
  configureProperties()

  val configFile = if (args.isNotEmpty()) Paths.get(args[0]) else Paths.get("config/loom.json")
  val config = Json().read<LoomConfig>(configFile)

  initLoomWorkspace()
  bootstrapLoom(config).run()
}

fun bootstrapLoom(config: LoomConfig): Loom {
  val aws = createAwsCloud(config.amazon)

  AwsBootstrap(aws).bootstrap()

  val fabricModels = InMemoryFabricModelDao()
  val fabricSpecs  = InMemoryFabricSpecDao()

  val loom = Loom(
      host = config.host,
      port = config.port,
      aws  = aws,
      json = Json(),
      fabricModelDao = fabricModels,
      fabricSpecDao  = fabricSpecs,
      fabricService  = TerraformAndKopsFabricService(aws, fabricModels, fabricSpecs)
  )

  return loom
}

private fun initLoomWorkspace() {
  Files.createDirectories(Paths.get(System.getProperty("user.home"), "loom-workspace"))
}

private fun configureProperties() {
  val props = Properties()
  props.load(FileInputStream("config/server.properties"))
  for ((name, value) in props) {
    System.setProperty(name.toString(), value.toString())
  }
}