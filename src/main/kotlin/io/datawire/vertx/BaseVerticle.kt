package io.datawire.vertx

import io.datawire.vertx.exception.InvalidConfigException
import io.datawire.vertx.jackson.DEFAULT_MODULES
import io.datawire.vertx.jackson.JacksonMessageCodec
import io.datawire.vertx.jackson.configureMapper
import io.vertx.core.AbstractVerticle
import io.vertx.core.Context
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.logging.LoggerFactory
import kotlin.reflect.KClass


abstract class BaseVerticle<out T: Config>(private val configClass: KClass<T>) : AbstractVerticle() {

    protected val logger = LoggerFactory.getLogger(javaClass)

    val config by lazy { createTypedConfig() }

    override fun init(vertx: Vertx, context: Context) {
        super.init(vertx, context)
        config
    }

    override fun getVertx(): Vertx = vertx

    override fun deploymentID(): String = context.deploymentID()

    @Throws(Exception::class)
    override fun start(startFuture: Future<Void>?) {
        start()
        startFuture!!.complete()
    }

    @Throws(Exception::class)
    override fun stop(stopFuture: Future<Void>?) {
        stop()
        stopFuture!!.complete()
    }

    @Throws(Exception::class)
    override fun start() { }

    @Throws(Exception::class)
    override fun stop() { }

    protected inline fun <reified T: Any> registerEventBusCodec() {
        vertx.eventBus().registerDefaultCodec(T::class.java, JacksonMessageCodec(T::class))
    }

    private fun createTypedConfig(): T {
        val config = try {
            fromJson(config(), configClass.java, validate = false)
        } catch (ex: Throwable) {
            throw InvalidConfigException(ex)
        }

        val violations = ValidatorFactory.validate(config)
        if (violations.isNotEmpty()) {
            throw InvalidConfigException(violations)
        }

        return config
    }

    private companion object {

        private val logger = LoggerFactory.getLogger(BaseVerticle.Companion::class.java)

        init {
            configureJackson()
        }

        private fun configureJackson() {
            for (mapper in setOf(Json.mapper, Json.prettyMapper)) {
                configureMapper(mapper, DEFAULT_MODULES)
            }

            logger.debug("Jackson object mappers configured")
        }
    }
}