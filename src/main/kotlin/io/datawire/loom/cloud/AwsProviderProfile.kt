package io.datawire.loom.cloud

import com.amazonaws.auth.*
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client


data class AwsProviderProfile(val name: String,
                              val region: String,
                              val credentials: AwsProviderCredentials? = null) {

    private val credentialsChain: AWSCredentialsProviderChain by lazy {
        val providers = mutableListOf(
                ProfileCredentialsProvider(),
                ProfileCredentialsProvider(name),
                EnvironmentVariableCredentialsProvider(),
                InstanceProfileCredentialsProvider.createAsyncRefreshingProvider(false))

        credentials?.let { providers += AWSStaticCredentialsProvider(BasicAWSCredentials(it.accessKey, it.secretKey)) }

        AWSCredentialsProviderChain(providers)
    }

    val s3: AmazonS3 by lazy             { AmazonS3Client.builder().withCredentials(credentialsChain).build() }
    val dynamodb: AmazonDynamoDB by lazy { AmazonDynamoDBClient.builder().withCredentials(credentialsChain).build() }
}