package io.datawire.vertx

import io.datawire.config.SimpleConfig
import io.datawire.config.ValidatableConfig
import io.datawire.test.BaseTestUsingVertx
import io.datawire.vertx.exception.InvalidConfigException
import io.vertx.core.DeploymentOptions
import io.vertx.ext.unit.TestContext
import org.junit.Test


class BaseVerticleTest : BaseTestUsingVertx() {

    class BaseVerticleUsingSimpleConfig : BaseVerticle<SimpleConfig>(SimpleConfig::class)
    class BaseVerticleUsingValidatedConfig : BaseVerticle<ValidatableConfig>(ValidatableConfig::class)

    @Test
    fun jsonConfig_BindsJsonToObjectCorrectly(ctx: TestContext) {
        val verticle = BaseVerticleUsingSimpleConfig()
        val options = DeploymentOptions().setConfig(SimpleConfig.VALID_CONFIG)
        vertx.deployVerticle(verticle, options, ctx.asyncAssertSuccess {
            val config = verticle.config
            ctx.assertEquals("Phil", config.name)
            ctx.assertEquals(27, config.age)
        })
    }

    @Test
    fun jsonConfig_ThrowsInvalidConfigException(ctx: TestContext) {
        val verticle = BaseVerticleUsingSimpleConfig()
        val options = DeploymentOptions().setConfig(SimpleConfig.INVALID_CONFIG)
        vertx.deployVerticle(verticle, options, ctx.asyncAssertFailure { ex ->
            ctx.assertTrue(ex is InvalidConfigException)
        })
    }

    @Test
    fun validConfig_BindsJsonToObjectCorrectly(ctx: TestContext) {
        val verticle = BaseVerticleUsingValidatedConfig()
        val options = DeploymentOptions().setConfig(ValidatableConfig.VALID_CONFIG)
        vertx.deployVerticle(verticle, options, ctx.asyncAssertSuccess {
            val config = verticle.config
            ctx.assertEquals("phil@example.org", config.email)
        })
    }

    @Test
    fun invalidConfig_ThrowsInvalidConfigException(ctx: TestContext) {
        val verticle = BaseVerticleUsingValidatedConfig()
        val options = DeploymentOptions().setConfig(ValidatableConfig.INVALID_CONFIG)
        vertx.deployVerticle(verticle, options, ctx.asyncAssertFailure { ex ->
            ctx.assertTrue(ex is InvalidConfigException)
            println(ex.message)
            ctx.assertTrue("Constraint violation for class ${ValidatableConfig::class.qualifiedName}['email']" in ex.message!!)
            ctx.assertTrue("Constraint violation for class ${ValidatableConfig::class.qualifiedName}['age']" in ex.message!!)
        })
    }
}