package io.datawire.loom.v2.setup

import com.amazonaws.services.dynamodbv2.model.*
import io.datawire.loom.v2.aws.AwsProvider
import io.vertx.core.logging.LoggerFactory


class Bootstrap(private val provider: AwsProvider) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun bootstrap() {
        logger.info("AWS bootstrap started")
        createStateStorageBucket()
        createTerraformLockTable()
        logger.info("AWS bootstrap completed")
    }

    private fun createStateStorageBucket() {
        val created = provider.createPrivateBucketIfNotExists(provider.stateStorageBucket, private = true, versioned = true)
        if (created) {
            logger.info("AWS S3 bucket for Loom state store created: {}", provider.stateStorageBucket)
        } else {
            logger.info("AWS S3 bucket for Loom state store existed already: {}", provider.stateStorageBucket)
        }
    }

    private fun createTerraformLockTable() {
        val createTable = CreateTableRequest()
                .withTableName("loom_terraform_state_lock")
                .withKeySchema(KeySchemaElement().withAttributeName("LockID").withKeyType(KeyType.HASH))
                .withAttributeDefinitions(AttributeDefinition().withAttributeName("LockID").withAttributeType("S"))
                .withProvisionedThroughput(ProvisionedThroughput(3, 3))

        val created = provider.createDynamoDbTableIfNotExists(createTable)
        if (created) {
            logger.info("AWS DynamoDB table for Loom terraform state locks created: {}", createTable.tableName)
        } else {
            logger.info("AWS DynamoDB table for Loom terraform state locks existed already: {}", createTable.tableName)
        }
    }
}