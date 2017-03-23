package io.datawire.vertx

import io.datawire.vertx.exception.InvalidConfigException
import io.datawire.vertx.jackson.DEFAULT_MODULES
import io.datawire.vertx.jackson.JacksonMessageCodec
import io.datawire.vertx.jackson.configureMapper
import io.vertx.core.*
import io.vertx.core.json.Json
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import kotlin.properties.Delegates
import kotlin.properties.Delegates.notNull
import kotlin.reflect.KClass


abstract class BaseVerticle<out T: Config>(private val configClass: KClass<T>) : Verticle {

    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    private var _vertx by notNull<Vertx>()
    protected var context by notNull<Context>()

    val config by lazy { createTypedConfig() }

    override fun init(vertx: Vertx?, context: Context?) {
        this._vertx  = vertx!!
        this.context = context!!

        config
    }

    override fun getVertx(): Vertx = _vertx

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
    open fun start() { }

    @Throws(Exception::class)
    open fun stop() { }

    protected inline fun <reified T: Any> registerEventBusCodec() {
        vertx.eventBus().registerDefaultCodec(T::class.java, JacksonMessageCodec(T::class))
    }

    private fun createTypedConfig(): T {
        val config = try {
            fromJson(context.config(), configClass.java, validate = false)
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