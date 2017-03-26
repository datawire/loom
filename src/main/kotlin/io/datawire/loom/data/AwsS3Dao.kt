package io.datawire.loom.data

import io.datawire.loom.aws.AwsProvider
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass


class AwsS3Dao<T: Any>(private val aws: AwsProvider,
                       private val daoType: KClass<T>,
                       _prefix: String) {

    private val bucket = aws.stateStorageBucket
    private val prefix = _prefix.toLowerCase()

    private val cache = ConcurrentHashMap<String, T>()

    fun delete(key: String) {
        val objectKey = createObjectKey(key)
        aws.newS3Client().deleteObject(bucket, objectKey)
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
        aws.newS3Client().putObject(bucket, objectKey, toJson(model))
        cache[objectKey] = model
    }

    fun listItems(): Collection<String> = aws.newS3Client().listObjectsV2(bucket).objectSummaries.map { it.key }

    private fun createObjectKey(key: String): String {
        return "$prefix/$key"
    }

    private fun getObjectAsStringOrNull(key: String): String? {
        return try {
            aws.newS3Client().getObjectAsString(bucket, key)
        } catch (any: Throwable) {
            null
        }
    }
}