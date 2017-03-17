package io.datawire.loom.cloud.aws

import com.amazonaws.services.dynamodbv2.model.*
import com.amazonaws.services.dynamodbv2.util.TableUtils
import com.amazonaws.services.s3.model.*
import io.datawire.loom.cloud.AwsProviderProfile
import io.vertx.core.logging.LoggerFactory


data class AwsStateStore(val bucket: String, val region: String = "us-east-1") {

    private val logger = LoggerFactory.getLogger(AwsStateStore::class.java)

    fun initialize(provider: AwsProviderProfile) {
        val s3 = provider.s3
        if (!s3.doesBucketExist(bucket)) {
            logger.info("Loom state storage 's3://{}' in '{}' does not exist. It will be created.", bucket, region)
            val createBucket = CreateBucketRequest(bucket, Region.fromValue(region))
            val bucket = s3.createBucket(createBucket.withCannedAcl(CannedAccessControlList.Private))
            val versionSupport = SetBucketVersioningConfigurationRequest(bucket.name, BucketVersioningConfiguration(BucketVersioningConfiguration.ENABLED))
            s3.setBucketVersioningConfiguration(versionSupport)
            logger.info("Loom state store created!")
        } else {
            logger.info("Loom state storage 's3://{}' in '{}' exists. It will NOT be created.", bucket, region)
        }

        val createTable = CreateTableRequest()
                .withTableName("terraform_state")
                .withKeySchema(KeySchemaElement().withAttributeName("LockID").withKeyType(KeyType.HASH))
                .withAttributeDefinitions(AttributeDefinition().withAttributeName("LockID").withAttributeType("S"))
                .withProvisionedThroughput(ProvisionedThroughput(1,1))

        TableUtils.createTableIfNotExists( provider.dynamodb, createTable)
        TableUtils.waitUntilExists( provider.dynamodb, createTable.tableName)
        logger.info("Loom Terraform DynamoDB lock table '{}' created!", createTable.tableName)
    }
}

fun main(args: Array<String>) {
    val stateStore = AwsStateStore("datawire-loom")
    stateStore.initialize(AwsProviderProfile("foo", "us-east-1"))
}

