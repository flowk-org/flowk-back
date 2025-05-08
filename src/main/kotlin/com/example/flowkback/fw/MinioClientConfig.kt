package com.example.flowkback.fw

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MinioClientConfig {
    @Bean
    fun minioClient(
        @Value("\${minio.endpoint}") endpoint: String,
        @Value("\${minio.access-key}") accessKey: String,
        @Value("\${minio.secret-key}") secretKey: String,
        @Value("\${minio.bucket-name}") defaultBucket: String
    ): MinioClient {
        return MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build()
    }
}