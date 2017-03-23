package io.datawire.loom.v2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.datawire.loom.v1.api.ApiConfig
import io.datawire.loom.v1.config.ExternalProgramConfig
import io.datawire.loom.v1.kops.KopsConfig
import io.datawire.loom.v1.terraform.TerraformConfig
import io.datawire.loom.v2.aws.AwsConfig
import io.datawire.loom.v2.auth.AuthProviderConfig
import io.datawire.loom.v2.auth.NoAuthProvider
import io.datawire.loom.v2.config.ExternalTool
import io.datawire.vertx.Config


@JsonIgnoreProperties(ignoreUnknown = true)
data class LoomConfig(val api            : ApiConfig          = ApiConfig(host = "0.0.0.0", port = 7000),
                      val authentication : AuthProviderConfig = NoAuthProvider(),
                      val amazon         : AwsConfig          = AwsConfig(null, null, null),
                      val kops           : ExternalTool       = ExternalTool(executable = "kops"),
                      val terraform      : ExternalTool       = ExternalTool(executable = "terraform")) : Config