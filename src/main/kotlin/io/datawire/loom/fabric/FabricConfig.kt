package io.datawire.loom.fabric


data class FabricConfig(
    val clusterCidr   : String = "30.0.0.0/16",
    val model         : String,
    val name          : String,
    val resourcesCidr : String = io.datawire.loom.fabric.computeResourcesCidr(clusterCidr)
) {

  fun normalize(): FabricConfig =
      copy(name = name.toLowerCase())
}

private fun computeResourcesCidr(clusterCidr: String): String {
  val cidrInfo = org.apache.commons.net.util.SubnetUtils(clusterCidr).info
  val addressParts = cidrInfo.address.split('.').map { it.toInt() }.toIntArray()

  if (addressParts[1] < 254) addressParts[1] += 1 else addressParts[1] -= 1

  return addressParts.joinToString(".", postfix = "/") + clusterCidr.substringAfterLast("/")
}
