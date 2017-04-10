package io.datawire.loom.proto.aws

import com.amazonaws.auth.*
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.client.builder.AwsSyncClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.*
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class AwsProvider(private val config: AwsConfig) {

  private val logger = LoggerFactory.getLogger(AwsProvider::class.java)

  /**
   * Credential lookup chain used by all AWS API clients.
   */
  private val credentialsChain: AWSCredentialsProviderChain

  /**
   * Unique AWS account identifier for the current IAM user credentials.
   */
  val accountId = newBlockingIamClient().user.user.arn.split(":")[4]

  /**
   * The name of the lock table used for Terraform state locking.
   */
  val lockTableName = "loom_terraform_state_lock"

  /**
   * The name of the S3 bucket where Loom state is stored.
   */
  val stateStorageBucketName = "loom-state-$accountId"

  /**
   * The Amazon S3 region used by the Amazon S3 state storage client.
   */
  val stateStorageBucketRegion: String

  /**
   * The Amazon S3 client configured to talk to the S3 state storage bucket.
   */
  val stateStorageClient: AmazonS3

  init {
    val providers = mutableListOf(
        ProfileCredentialsProvider(),
        EnvironmentVariableCredentialsProvider(),
        InstanceProfileCredentialsProvider.createAsyncRefreshingProvider(false))

    if (config.accessKey != null && config.secretKey != null) {
      providers += AWSStaticCredentialsProvider(BasicAWSCredentials(config.accessKey, config.secretKey))
    }

    credentialsChain = AWSCredentialsProviderChain(providers)

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
    val tempS3 = newS3Client()
    stateStorageBucketRegion = try {
      val region = tempS3.getBucketLocation(stateStorageBucketName)
      when (region) {
        "US" -> "us-east-1"
        else -> region
      }
    } catch (s3ex: AmazonS3Exception) {
      tempS3.regionName
    }

    stateStorageClient = AmazonS3ClientBuilder.standard()
        .withRegion(Regions.fromName(stateStorageBucketRegion))
        .withCredentials(credentialsChain)
        .build()
  }

  private inline fun <reified T : AwsSyncClientBuilder<*, *>> configure(builder: T): T {
    builder.withCredentials(credentialsChain)
    if (config.region != null) {
      builder.withRegion(Regions.fromName(config.region))
    }

    return builder
  }

  fun newBlockingIamClient(): AmazonIdentityManagement =
      configure<AmazonIdentityManagementClientBuilder>(AmazonIdentityManagementClientBuilder.standard()).build()

  fun newS3Client(): AmazonS3 = configure<AmazonS3ClientBuilder>(AmazonS3ClientBuilder.standard()).build()

  fun newBlockingDynamoClient(): AmazonDynamoDB
      = configure<AmazonDynamoDBClientBuilder>(AmazonDynamoDBClientBuilder.standard()).build()

  fun createPrivateBucketIfNotExists(name: String, private: Boolean = true, versioned: Boolean = false): Boolean {
    val s3 = stateStorageClient
    return if (!s3.doesBucketExist(name)) {
      val createBucket = CreateBucketRequest(name).apply {
        if (private) {
          withCannedAcl(CannedAccessControlList.Private)
        }
      }

      val bucket = s3.createBucket(createBucket)
      if (versioned) {
        val versioningConfig = BucketVersioningConfiguration(BucketVersioningConfiguration.ENABLED)
        val request = SetBucketVersioningConfigurationRequest(bucket.name, versioningConfig)
        s3.setBucketVersioningConfiguration(request)
      }

      true
    } else {
      false
    }
  }

  fun createDynamoDbTableIfNotExists(createTable: CreateTableRequest): Boolean {
    return try {
      newBlockingDynamoClient().createTable(createTable)
      true
    } catch (ex: ResourceInUseException) {
      false
    }
  }
}

fun symlinkAwsConfig(link: Path): Path? {
  val awsConfig = Paths.get(System.getProperty("user.home"), ".aws")
  return if (Files.exists(awsConfig) && !Files.isSymbolicLink(link)) {
    Files.createSymbolicLink(link, awsConfig)
  } else {
    null
  }
}