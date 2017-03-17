package io.datawire.loom

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.datawire.loom.api.ApiConfig
import io.datawire.loom.config.ExternalProgramConfig
import io.datawire.loom.kops.KopsConfig
import io.datawire.loom.terraform.TerraformConfig
import io.datawire.vertx.Config


@JsonIgnoreProperties(ignoreUnknown = true)
data class LoomConfig(val api       : ApiConfig,
                      val kops      : KopsConfig,
                      val terraform : TerraformConfig) : Config