package io.datawire.loom.dev.aws

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.Regions
import org.junit.Test
import org.assertj.core.api.Assertions.*

class AwsCloudTest {

  private val aws = AwsCloud(
      "does-not-matter",
      "us-east-1",
      DefaultAWSCredentialsProviderChain(),
      "us-east-1",
      "does-not-matter")

  @Test
  fun isUsableRegion_givenInvalidRegion_returnsFalse() {
    assertThat(aws.isUsableRegion("Kirby's Dreamland")).isFalse()
  }

  @Test
  fun isUsableRegion_givenValidRegions_returnsTrue() {
    for (r in Regions.values().map { it.name }) {
      assertThat(aws.isUsableRegion(r)).isTrue()
    }
  }

  /**
   * Kubernetes barely runs on t2.nano systems so we prevent Loom users from using this instance type.
   */
  @Test
  fun isAllowedMasterType_giveT2Nano_returnsFalse() {
    assertThat(aws.isUsableMasterType("t2.nano")).isFalse()
  }

  /**
   * Kubernetes barely runs on t2.micro systems so we prevent Loom users from using this instance type.
   */
  @Test
  fun isAllowedMasterType_giveT2Micro_returnsFalse() {
    assertThat(aws.isUsableMasterType("t2.micro")).isFalse()
  }
}
