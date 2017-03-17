package io.datawire.loom.fabric

import io.datawire.test.BaseTest
import io.datawire.vertx.validate
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.kotlin.core.json.get

import org.assertj.core.api.Assertions.*
import org.junit.Test
import javax.validation.ConstraintViolationException


class FabricSpecTest : BaseTest() {

    @Test
    fun bindFromJsonWithNullName_BindsWithDefaultSetForName() {
//        val json = JsonObject().put("name", "foobar")
//        val spec = fromJson<FabricSpec>(json)
//        assertThat(spec.name).isEqualTo("foobar")
    }

    @Test
    fun nameIsNull_throwsConstraintViolationException() {
//        val spec = FabricSpec(mapOf("name" to null, "model" to "test"))
//
//        val thrown = catchThrowable { validate(spec) }
//        assertThat(thrown).isInstanceOf(ConstraintViolationException::class.java)
//
//        val cve = thrown as ConstraintViolationException
//        assertThat(cve.constraintViolations).hasSize(1)
//
//        val cv = cve.constraintViolations.first()
    }

    @Test
    fun nameDoesNotMatchRegex_throwsConstraintViolationException() {
//        val spec = FabricSpec(mapOf("name" to "1numbercannotbefirst", "model" to "foo"))
//
//        val thrown = catchThrowable { validate(spec) }
//        assertThat(thrown).isInstanceOf(ConstraintViolationException::class.java)
//
//        val cve = thrown as ConstraintViolationException
//        assertThat(cve.constraintViolations).hasSize(1)
//
//        val cv = cve.constraintViolations.first()
    }
}