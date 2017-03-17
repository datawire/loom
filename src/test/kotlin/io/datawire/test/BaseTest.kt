package io.datawire.test

import com.fasterxml.jackson.module.kotlin.readValue
import io.datawire.vertx.jackson.DEFAULT_MODULES
import io.datawire.vertx.jackson.configureMapper
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.http.HttpClientRequest
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.LocalMap
import io.vertx.ext.unit.junit.Timeout
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.runner.RunWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.net.ServerSocket
import java.net.URLEncoder
import java.nio.charset.Charset
import java.time.Instant
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference


@RunWith(VertxUnitRunner::class)
abstract class BaseTest {

    @get:Rule
    val timeoutRule: Timeout = Timeout.seconds(60)

    companion object {
        @JvmStatic
        @BeforeClass
        fun beforeSetup() {
            // Enforce that these tests execute in UTC. MCP servers operate in UTC.
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

            System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")
        }
    }

    protected val jsonMapper = configureMapper(Json.prettyMapper, DEFAULT_MODULES)

    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    // -------------------------------------------------------------------------------------------------------------------
    // Useful container types
    // -------------------------------------------------------------------------------------------------------------------

    // -------------------------------------------------------------------------------------------------------------------
    // Network utilities
    // -------------------------------------------------------------------------------------------------------------------

    /**
     * Acquires a random available port. It's not perfect for avoiding port collisions but is "good enough" for most
     * serious uses.
     *
     * @return the available port to use.
     */
    protected fun reserveListenPort(): Int {
        val socket = ServerSocket(0)
        val port = socket.localPort
        socket.close()
        return port
    }

    // -------------------------------------------------------------------------------------------------------------------
    // HTTP utilities
    // -------------------------------------------------------------------------------------------------------------------

    protected fun configureCommon(req: HttpClientRequest, token: String) {
        req.apply {
            putHeader(HttpHeaders.ACCEPT, "application/json")
            putHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
        }
    }

    protected fun urlEncode(text: String, encoding: Charset = Charsets.UTF_8): String {
        return URLEncoder.encode(text, encoding.name())
    }

    // -------------------------------------------------------------------------------------------------------------------
    // Vert.x SharedData utilities
    // -------------------------------------------------------------------------------------------------------------------

    protected fun <A, B> localMap(vertx: Vertx, name: String): LocalMap<A, B?> {
        return vertx.sharedData().getLocalMap<A, B>(name)
    }

    // -------------------------------------------------------------------------------------------------------------------
    // Vert.x EventBus utilities
    // -------------------------------------------------------------------------------------------------------------------

    protected fun publish(vertx: Vertx, to: String, message: Any, options: DeliveryOptions = DeliveryOptions()) {
        vertx.eventBus().publish(to, message, options)
    }

    // -------------------------------------------------------------------------------------------------------------------
    // Resources utilities
    // -------------------------------------------------------------------------------------------------------------------

    fun resourceStream(name: String): InputStream {
        return this.javaClass.getResourceAsStream("/$name")
    }

    fun resource(name: String, charset: Charset = Charsets.UTF_8): String {
        return resourceStream(name).bufferedReader(charset).readText()
    }

    protected fun resourceJsonArray(name: String) = JsonArray(resource(name))

    protected fun resourceJsonObject(name: String) = JsonObject(resource(name))

    // -------------------------------------------------------------------------------------------------------------------
    // Database / JDBC utilities
    // -------------------------------------------------------------------------------------------------------------------


    // -------------------------------------------------------------------------------------------------------------------
    // JSON utilities
    // -------------------------------------------------------------------------------------------------------------------

    /**
     * Deserialize a string containing JSON to specific type [T].
     *
     * @param text the [String] to deserialize.
     */
    protected inline fun <reified T: Any> fromJson(text: String): T {
        return Json.prettyMapper.readValue(text)
    }

    /**
     * Deserialize a [JsonObject] to a specific type [T].
     *
     * @param json the [JsonObject] to deserialize.
     */
    protected inline fun <reified T: Any> fromJson(json: JsonObject): T = fromJson(json.encodePrettily())

    /**
     * Deserialize a [JsonArray] to a specific type [T].
     *
     * @param json the [JsonArray] to deserialize.
     */
    protected inline fun <reified T: Any> fromJson(json: JsonArray): T = fromJson(json.encodePrettily())

    /**
     * Serialize a type into JSON.
     *
     * @param item the instance of <T> to serialize.
     */
    protected fun <T: Any> toJson(item: T): String = Json.prettyMapper.writeValueAsString(item)

    // -------------------------------------------------------------------------------------------------------------------
    // Vertx utilities
    // -------------------------------------------------------------------------------------------------------------------

    /**
     * A hook method for subclasses to provide custom [VertxOptions] before the vert.x server is started.
     */
    protected open fun provideVertxOptions(clustered: Boolean): VertxOptions {
        return VertxOptions().setClustered(clustered)
    }

    /**
     * Create the [Vertx] instance that will be used by the test. When constructing a clustered [Vertx] this method will
     * block for upto a minute in order to allow the [Vertx] instance to initialize.
     */
    protected open fun createVertx(clustered: Boolean): Vertx {
        val vertxOptions = provideVertxOptions(clustered)

        if (clustered && (clustered != vertxOptions.isClustered)) {
            logger.warn("Class has property 'clustered' = true, but 'VertxOptions#isClustered' = false."
                    + " Check overridden implementation of #provideVertxOptions()?")
        }

        return if (clustered) {
            val latch = CountDownLatch(1)
            val vertx = AtomicReference<Vertx?>()
            Vertx.clusteredVertx(vertxOptions) { clustering ->
                when {
                    clustering.succeeded() -> {
                        vertx.set(clustering.result())
                        latch.countDown()
                    }
                    else -> throw RuntimeException("Acquire clustered vertx failed")
                }
            }

            try {
                latch.await(1L, TimeUnit.MINUTES)
                return vertx.get()!!
            } catch (interrupt: InterruptedException) { throw RuntimeException(interrupt) }
        } else {
            Vertx.vertx(vertxOptions)
        }
    }
}