package io.datawire.loom.core.aws

import com.amazonaws.auth.*
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.client.builder.AwsSyncClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder
import com.amazonaws.services.route53.AmazonRoute53
import com.amazonaws.services.route53.AmazonRoute53ClientBuilder
import com.amazonaws.services.route53.model.ListHostedZonesByNameRequest
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.AmazonS3Exception
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest
import io.datawire.loom.core.Json


class AwsCloud(
    private val accountId: String,
    private val region: String?,
    private val credentials: AWSCredentialsProviderChain,
    val stateStorageBucketRegion: String,
    val stateStorageBucketName: String
) {

  private val json = Json()

  /**
   * The name of the lock table used for Terraform state locking.
   */
  val lockTableName = "loom_terraform_state_lock"

  /**
   * The Amazon S3 client configured to talk to the S3 state storage bucket.
   */
  val stateStorageClient: AmazonS3 = AmazonS3ClientBuilder.standard()
      .withRegion(Regions.fromName(stateStorageBucketRegion))
      .withCredentials(credentials)
      .build()

  private val regions = Regions.values().associateBy { it.name.toLowerCase() }

  // TODO => Explore a mechanism for dynamically populating this information in the future.
  private val usableInstanceTypes
      = json.read<Set<String>>(javaClass.getResourceAsStream("/usable-instance-types.json").bufferedReader().readText())

  fun isUsableRegion(region: String): Boolean = region.toLowerCase() in regions

  fun isUsableMasterType(type: String) = type.toLowerCase() in (usableInstanceTypes - "t2.micro" - "t2.nano")

  fun isUsableNodeType(type: String) = type.toLowerCase() in usableInstanceTypes

  fun isOwnedRoute53Domain(name: String): Boolean {
    val route53 = newRoute53Client()
    val request = ListHostedZonesByNameRequest().withDNSName(name).withMaxItems("1")

    val result = route53.listHostedZonesByName(request)
    return when {
      result.hostedZones.isEmpty() -> false
      result.hostedZones.map { it.name.toLowerCase().substringBeforeLast(".") }.contains(name.toLowerCase()) -> true
      else -> false
    }
  }

  fun newRoute53Client(): AmazonRoute53 =
      configure<AmazonRoute53ClientBuilder>(AmazonRoute53ClientBuilder.standard()).build()

  fun newS3Client(): AmazonS3 = configure<AmazonS3ClientBuilder>(AmazonS3ClientBuilder.standard()).build()

  fun newBlockingIamClient(): AmazonIdentityManagement =
      configure<AmazonIdentityManagementClientBuilder>(AmazonIdentityManagementClientBuilder.standard()).build()

  fun newBlockingDynamoClient(): AmazonDynamoDB
      = configure<AmazonDynamoDBClientBuilder>(AmazonDynamoDBClientBuilder.standard()).build()

  /**
   * A generic mechanism for configuring AWS SDK clients with both a region and
   */
  private inline fun <reified T : AwsSyncClientBuilder<*, *>> configure(builder: T): T {
    builder.withCredentials(credentials)

    return if (region != null) {
      builder.withRegion(Regions.fromName(region)) as T
    } else {
      builder
    }
  }
}

/**
 * Constructs a new instance of [AwsCloud].
 *
 * @param config configuration used to produce an instance of [AwsCloud].
 */
fun createAwsCloud(config: AwsConfig): AwsCloud {
  val credentialProviders = mutableListOf(
      ProfileCredentialsProvider(),
      EnvironmentVariableCredentialsProvider(),
      InstanceProfileCredentialsProvider.getInstance()
  )

  if (config.accessKey != null && config.secretKey != null) {
    credentialProviders += AWSStaticCredentialsProvider(BasicAWSCredentials(config.accessKey, config.secretKey))
  }

  val credentials = AWSCredentialsProviderChain(credentialProviders)
  val accountId = fetchAwsAccountId(credentials)
  val stateStorageBucketName = "loom-state-$accountId"
  val stateStorageBucketRegion = fetchS3StateStorageBucketRegion(stateStorageBucketName, credentials)

  return AwsCloud(accountId, config.region, AWSCredentialsProviderChain(credentialProviders), stateStorageBucketRegion, stateStorageBucketName)
}

/**
 * Discovery the region where the Loom state store is located.
 *
 * @param stateStorageBucketName name of the S3 bucket where state is stored.
 * @param credentials a credentials chain to get AWS credentials to communicate with the AWS API.
 * @return the region where the bucket is located
 */
private fun fetchS3StateStorageBucketRegion(
    stateStorageBucketName: String,
    credentials: AWSCredentialsProviderChain
): String {

  //
  // If Loom was run by Alice in 'us-east-1' first then the state storage bucket is going to already exist for
  // Bob. When Bob later comes along and runs Loom but provides credentials pointing to another region other than
  // us-east-1 then he will not have any of the previous state. This bit of S3 juggling helps address this
  // problem.
  //
  // If the bucket EXISTS then that will be the location where Loom stores state and the state storage S3
  // client will be connected to that AWS region and use that bucket.
  //
  // If the bucket does NOT EXIST then we can (probably) assume this is the first Loom user for that AWS account
  // and create the bucket to the region where the S3 client is connected according to their AWS client config.
  //
  // This scenario can occur under a couple common situations:
  //
  //      1. More than one user runs Loom on their local workstation, for example, during the Getting Started
  //         tutorial.
  //
  //      2. A user runs Loom on their local workstation which has an AWS config setup to talk to 'us-east-1' but
  //         they decide to install Loom for production and run it in 'us-east-2'.
  //
  //      3. A Loom already running in production is transitioned from one region to another region.
  //
  val tempS3 = AmazonS3ClientBuilder.standard().withCredentials(credentials).build()
  val result = try {
    val region = tempS3.getBucketLocation(stateStorageBucketName)
    when (region) {
      "US" -> "us-east-1"
      else -> region
    }
  } catch (s3ex: AmazonS3Exception) { tempS3.regionName }

  return result
}

/**
 * Discover the twelve digit account identifier associated with the API consumers AWS account.
 *
 * @param credentials a credentials chain to get AWS credentials to communicate with the AWS API.
 * @return the twelve digit account identifier.
 */
private fun fetchAwsAccountId(credentials: AWSCredentialsProviderChain): String {
  val sts = AWSSecurityTokenServiceClientBuilder.standard().withCredentials(credentials).build()
  return sts.getCallerIdentity(GetCallerIdentityRequest()).account
}