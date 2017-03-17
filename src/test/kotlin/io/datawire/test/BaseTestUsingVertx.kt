package io.datawire.test

import io.vertx.core.Vertx
import io.vertx.ext.unit.TestContext
import org.junit.After
import org.junit.Before
import io.vertx.ext.unit.junit.RunTestOnContext
import org.junit.Rule


abstract class BaseTestUsingVertx(private val clustered: Boolean = false) : BaseTest() {

    @get:Rule
    val rule = RunTestOnContext({ createVertx(clustered) })

    /**
     * Lazy initialized so that [rule] is initialized before access.
     */
    val vertx: Vertx by lazy { rule.vertx() }

    @Before
    open fun setup(ctx: TestContext) { }

    @After
    open fun teardown(ctx: TestContext) {
        rule.vertx().close(ctx.asyncAssertSuccess())
    }
}