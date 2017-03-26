package io.datawire.loom.aws

import com.amazonaws.auth.*
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.client.builder.AwsSyncClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.*
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

    private val credentialsChain: AWSCredentialsProviderChain by lazy {
        val providers = mutableListOf(
                ProfileCredentialsProvider(),
                EnvironmentVariableCredentialsProvider(),
                InstanceProfileCredentialsProvider.createAsyncRefreshingProvider(false))

        if (config.accessKey != null && config.secretKey != null) {
            providers += AWSStaticCredentialsProvider(BasicAWSCredentials(config.accessKey, config.secretKey))
        }

        AWSCredentialsProviderChain(providers)
    }

    val accountId = newBlockingIamClient().user.user.arn.split(":")[4]
    val stateStorageBucket = "loom-state-$accountId"
    val lockTableName      = "loom_terraform_state_lock"

    private inline fun <reified T: AwsSyncClientBuilder<*,*>>configure(builder: T): T {
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
        val s3 = newS3Client()
        return if (!s3.doesBucketExist(name)) {
            val createBucket = CreateBucketRequest(name, Region.fromValue("us-east-1")).apply {
                if (private) {
                    withCannedAcl(CannedAccessControlList.Private)
                }
            }

            val bucket = s3.createBucket(createBucket)
            if (versioned) {
                val versioningConfig = BucketVersioningConfiguration(BucketVersioningConfiguration.ENABLED)
                val request          = SetBucketVersioningConfigurationRequest(bucket.name, versioningConfig)
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