package io.datawire.loom.core.aws

import com.amazonaws.services.dynamodbv2.model.*
import com.amazonaws.services.s3.model.BucketVersioningConfiguration
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.CreateBucketRequest
import com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest
import org.slf4j.LoggerFactory


class AwsBootstrap(private val aws: AwsCloud) {

  private val logger = LoggerFactory.getLogger(javaClass)

  fun bootstrap() {
    logger.info("== AWS bootstrap started")
    createStateStorageBucket()
    createTerraformLockTable()
    logger.info(">> AWS bootstrap completed")
  }

  private fun createPrivateBucketIfNotExists(name: String, private: Boolean = true, versioned: Boolean = false): Boolean {
    val s3 = aws.stateStorageClient
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

  private fun createDynamoDbTableIfNotExists(createTable: CreateTableRequest): Boolean {
    return try {
      aws.newBlockingDynamoClient().createTable(createTable)
      true
    } catch (ex: ResourceInUseException) {
      false
    }
  }

  private fun createStateStorageBucket() {
    val created = createPrivateBucketIfNotExists(
        name = aws.stateStorageBucketName,
        private = true,
        versioned = true
    )

    if (created) {
      logger.info("== AWS S3 bucket for Loom state store created: {} ({})",
                  aws.stateStorageBucketName,
                  aws.stateStorageBucketRegion)

    } else {
      logger.info("== AWS S3 bucket for Loom state store existed already: {} ({})",
                  aws.stateStorageBucketName,
                  aws.stateStorageBucketRegion)
    }
  }

  private fun createTerraformLockTable() {
    val createTable = CreateTableRequest()
        .withTableName(aws.lockTableName)
        .withKeySchema(KeySchemaElement().withAttributeName("LockID").withKeyType(KeyType.HASH))
        .withAttributeDefinitions(AttributeDefinition().withAttributeName("LockID").withAttributeType("S"))
        .withProvisionedThroughput(ProvisionedThroughput(3, 3))

    val created = createDynamoDbTableIfNotExists(createTable)
    if (created) {
      logger.info("== AWS DynamoDB table for Loom terraform state locks created: {}", createTable.tableName)
    } else {
      logger.info("== AWS DynamoDB table for Loom terraform state locks existed already: {}", createTable.tableName)
    }
  }
}