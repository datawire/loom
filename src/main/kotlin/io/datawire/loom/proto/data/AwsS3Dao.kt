package io.datawire.loom.proto.data

import com.amazonaws.services.s3.model.ListObjectsV2Request
import io.datawire.loom.proto.aws.AwsProvider
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass


class AwsS3Dao<T: Any>(
    private val aws: AwsProvider,
    private val daoType: KClass<T>,
                       _prefix: String) {

    private val bucket = aws.stateStorageBucketName
    private val prefix = _prefix.toLowerCase()
    private val s3     = aws.stateStorageClient

    private val cache = ConcurrentHashMap<String, T>()

    fun delete(key: String) {
        val objectKey = createObjectKey(key)
        s3.deleteObject(bucket, objectKey)
        cache.remove(objectKey)
    }

    fun get(key: String): T? {
        val objectKey = createObjectKey(key)
        return cache[objectKey] ?: getObjectAsStringOrNull(objectKey)?.let {
            val model = fromJson(it, daoType)
            cache.putIfAbsent(objectKey, model) ?: model
        }
    }

    fun put(key: String, model: T) {
        val objectKey = createObjectKey(key)
        s3.putObject(bucket, objectKey, toJson(model))
        cache[objectKey] = model
    }

    fun listItems(prefix: String? = null): List<String> {
        val list = ListObjectsV2Request()
            .withBucketName(bucket)
            .withPrefix(prefix)

        return s3.listObjectsV2(list).objectSummaries.map { it.key.replace(prefix ?: "", "") }.filterNot { it.endsWith(".tfstate") }
    }

    private fun createObjectKey(key: String): String {
        return "$prefix/$key"
    }

    private fun getObjectAsStringOrNull(key: String): String? {
        return try {
            s3.getObjectAsString(bucket, key)
        } catch (any: Throwable) {
            null
        }
    }
}