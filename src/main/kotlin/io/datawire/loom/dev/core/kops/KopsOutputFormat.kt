package io.datawire.loom.dev.core.kops


enum class KopsOutputFormat {

  /**
   * Use Kops internal ("direct") configuration mechanism. No intermediary format should be generated when this format
   * is specified.
   */
  DIRECT,

  /**
   * Use Kops to generate Terraform-compatible configuration.
   */
  TERRAFORM
}