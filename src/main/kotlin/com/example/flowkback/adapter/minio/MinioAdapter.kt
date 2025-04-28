package com.example.flowkback.adapter.minio

import io.minio.*
import io.minio.http.Method
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.InputStream
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException

@Component
class MinioAdapter(
    @Value("\${minio.endpoint}") private val endpoint: String,
    @Value("\${minio.access-key}") private val accessKey: String,
    @Value("\${minio.secret-key}") private val secretKey: String,
    @Value("\${minio.bucket-name}") private val defaultBucket: String
) {
    private val minioClient: MinioClient by lazy {
        MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build()
    }

    /**
     * Upload file to MinIO
     */
    fun uploadFile(
        inputStream: InputStream,
        fileName: String,
        contentType: String = "application/octet-stream",
        bucketName: String = defaultBucket
    ): String {
        try {
            createBucketIfNotExists(bucketName)

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(fileName)
                    .stream(inputStream, -1, 10485760) // 10MB part size
                    .contentType(contentType)
                    .build()
            )

            return getFileUrl(bucketName, fileName)
        } catch (e: Exception) {
            throw MinioOperationException("Failed to upload file '$fileName' to bucket '$bucketName'", e)
        }
    }

    /**
     * Download file from MinIO
     */
    fun downloadFile(
        fileName: String,
        bucketName: String = defaultBucket
    ): InputStream {
        return try {
            minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(fileName)
                    .build()
            )
        } catch (e: Exception) {
            throw MinioOperationException("Failed to download file '$fileName' from bucket '$bucketName'", e)
        }
    }

    /**
     * Get public URL for file
     */
    fun getFileUrl(
        fileName: String,
        bucketName: String = defaultBucket,
        expiresInSeconds: Int = 604800 // 7 days
    ): String {
        return try {
            minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .`object`(fileName)
                    .expiry(expiresInSeconds)
                    .build()
            )
        } catch (e: Exception) {
            throw MinioOperationException("Failed to generate URL for file '$fileName'", e)
        }
    }

    /**
     * Delete file from MinIO
     */
    fun deleteFile(
        fileName: String,
        bucketName: String = defaultBucket
    ) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(fileName)
                    .build()
            )
        } catch (e: Exception) {
            throw MinioOperationException("Failed to delete file '$fileName' from bucket '$bucketName'", e)
        }
    }

    /**
     * Check if bucket exists and create if not
     */
    private fun createBucketIfNotExists(bucketName: String) {
        try {
            val exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            )

            if (!exists) {
                println("Creating bucket $bucketName")
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
                )

                // Проверьте, что бакет действительно создался
                if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                    throw IllegalStateException("Failed to verify bucket creation")
                }
            }
        } catch (e: Exception) {
            throw MinioOperationException("Bucket operation failed for '$bucketName'", e)
        }
    }
}

class MinioOperationException(message: String, cause: Throwable?) : RuntimeException(message, cause)