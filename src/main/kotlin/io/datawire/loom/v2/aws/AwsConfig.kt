package io.datawire.loom.v2.aws

import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer


/**
 * Configuration for underlying tools and API clients that need to communicate with AWS interfaces. This object does not
 * generally need to be configured because most of the information is inferred or passed along via other means, for
 * example, when running on an EC2 instance the credentials are provided by the EC2 metadata subsystem.
 *
 * @property accessKey the AWS API access key to configure clients to use.
 * @property secretKey the AWS API secret key to configure clients to use.
 * @property region the AWS API region to communicate with.
 */
class AwsConfig(val accessKey: String?,
                val secretKey: String?,
                val region: String?)
