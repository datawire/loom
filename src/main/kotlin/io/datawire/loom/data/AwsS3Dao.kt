package io.datawire.loom.data

import io.datawire.loom.aws.AwsProvider
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass


class AwsS3Dao<T: Any>(private val aws: AwsProvider,
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

    fun listItems(): Collection<String> = s3.listObjectsV2(bucket).objectSummaries.map { it.key }

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