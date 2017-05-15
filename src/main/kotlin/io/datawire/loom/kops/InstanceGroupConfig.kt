package io.datawire.loom.kops


data class InstanceGroupConfig(val metadata: Metadata, val spec: InstanceGroupSpec) {
  val kind       = "InstanceGroup"
  val apiVersion = "kops/v1alpha2"
}
