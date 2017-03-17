package io.datawire.loom.fabric

import io.datawire.test.BaseTest
import org.assertj.core.api.Assertions.*
import org.junit.Test


class FabricModelTest : BaseTest() {

    @Test
    fun bindFromValidJson() {
        val model = FabricModel("foo", "bar", emptyList())

//        val json = resourceJsonObject("${FabricModel::class.simpleName}_basic.json")
//        val bound = fromJson<FabricModel>(json)
//
//        assertThat(bound.name).isEqualTo("basic")
//        assertThat(bound.masterType).isEqualTo("root.masterType")
//        assertThat(bound.nodeGroups).hasSize(1)
//
//        val nodeGroup = bound.nodeGroups[0]
//        assertThat(nodeGroup.name).isEqualTo("main")
//        assertThat(nodeGroup.nodeCount).isEqualTo(1)
//        assertThat(nodeGroup.nodeType).isEqualTo("main.nodeType")
//
//        println(bound)
    }
}