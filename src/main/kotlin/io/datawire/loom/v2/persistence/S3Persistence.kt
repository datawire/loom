package io.datawire.loom.v2.persistence

import com.amazonaws.services.s3.model.ListObjectsV2Request
import io.datawire.loom.v2.aws.AwsProvider
import io.vertx.core.logging.LoggerFactory


class S3Persistence(private val provider: AwsProvider) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val bucket = provider.stateStorageBucket

    fun writeTextObject(key: String, data: String) {
        logger.debug("Attempting to write data to s3://{}/{}", bucket, key)
        provider.newS3Client().putObject(bucket, key, data)
    }

    fun listObjects(prefix: String): List<String> {
        val s3 = provider.newS3Client()
        return s3.listObjectsV2(ListObjectsV2Request().withBucketName(provider.stateStorageBucket).withPrefix(prefix)).objectSummaries.map { it.key }
    }

    fun getObject(key: String): String {
        return provider.newS3Client().getObjectAsString(bucket, key)
    }

    fun deleteObject(key: String) {
        val s3 = provider.newS3Client()
        return s3.deleteObject(provider.stateStorageBucket, key)
    }
}