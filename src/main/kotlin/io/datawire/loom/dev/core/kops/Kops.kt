package io.datawire.loom.dev.core.kops


class Kops(
    private val executable: String,
    private val stateStore: String
) {

  fun createCluster() {

  }

  fun deleteCluster(name: String) {

  }

  private fun kops(vararg args: String) = (arrayOf(executable) + args).toMutableList()
}