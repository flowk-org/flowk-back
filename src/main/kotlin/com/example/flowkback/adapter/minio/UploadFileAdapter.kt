package com.example.flowkback.adapter.minio

import com.example.flowkback.app.api.UploadFileOutbound
import io.minio.*
import io.minio.http.Method
import io.minio.messages.Tags
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class UploadFileAdapter(private val minioClient: MinioClient) : UploadFileOutbound {
    private val defaultBucket: String = "models"

    override fun upload(
        inputStream: InputStream,
        fileName: String,
        contentType: String,
        bucketName: String,
        tags: Map<String, String>
    ): String {
        try {
            // 1. Ensure bucket exists with retries
            ensureBucketReady(bucketName)

            // 2. Read content to byte array for reliable upload
            val contentBytes = inputStream.readAllBytes()
            val contentLength = contentBytes.size.toLong()

            // 3. Upload with verification
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(fileName)
                    .tags(Tags.newBucketTags(tags))
                    .stream(contentBytes.inputStream(), contentLength, -1)
                    .contentType(contentType)
                    .build()
            )

            // 4. Verify upload
            verifyObjectExists(bucketName, fileName)

            // 5. Return public URL
            return getFileUrl(fileName, bucketName).also {
                println("Successfully uploaded $fileName to $bucketName")
            }
        } catch (e: Exception) {
            throw MinioOperationException("Failed to upload file '$fileName' to bucket '$bucketName'", e)
        }
    }

    /**
     * Comprehensive bucket preparation with policy setup
     */
    private fun ensureBucketReady(bucketName: String) {
        try {
            // 1. Check if bucket exists with retries
            if (!checkBucketExistsWithRetry(bucketName, maxAttempts = 3)) {
                println("Creating bucket $bucketName")
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build())

                // Wait for bucket to be ready
                if (!checkBucketExistsWithRetry(bucketName, maxAttempts = 5)) {
                    throw IllegalStateException("Bucket $bucketName not available after creation")
                }

                // Set public read policy
                setBucketPublicPolicy(bucketName)
            }
        } catch (e: Exception) {
            throw MinioOperationException("Bucket preparation failed for '$bucketName'", e)
        }
    }

    /**
     * Verify object exists after upload
     */
    private fun verifyObjectExists(bucketName: String, fileName: String) {
        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(fileName)
                    .build()
            )
        } catch (e: Exception) {
            throw MinioOperationException("Upload verification failed for '$fileName'", e)
        }
    }

    /**
     * Bucket existence check with retries
     */
    private fun checkBucketExistsWithRetry(bucketName: String, maxAttempts: Int): Boolean {
        repeat(maxAttempts) { attempt ->
            try {
                if (minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                    return true
                }
                if (attempt < maxAttempts - 1) Thread.sleep((500 * (attempt + 1)).toLong())
            } catch (e: Exception) {
                println("Bucket check attempt ${attempt + 1} failed: ${e.message}")
                if (attempt == maxAttempts - 1) throw e
            }
        }
        return false
    }

    /**
     * Set public read policy for bucket
     */
    private fun setBucketPublicPolicy(bucketName: String) {
        val policy = """
        {
            "Version": "2012-10-17",
            "Statement": [
                {
                    "Effect": "Allow",
                    "Principal": {"AWS": ["*"]},
                    "Action": [
                        "s3:GetObject",
                        "s3:PutObject",
                        "s3:DeleteObject",
                        "s3:ListBucket"
                    ],
                    "Resource": [
                        "arn:aws:s3:::$bucketName",
                        "arn:aws:s3:::$bucketName/*"
                    ]
                }
            ]
        }
        """.trimIndent()

        try {
            minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(policy)
                    .build()
            )
        } catch (e: Exception) {
            println("Warning: Failed to set bucket policy: ${e.message}")
        }
    }

    fun getFileUrl(
        fileName: String,
        bucketName: String = defaultBucket,
        expiresInSeconds: Int = 604800
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
}
