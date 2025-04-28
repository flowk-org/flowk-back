package com.example.flowkback.adapter.minio

import com.example.flowkback.app.api.UploadFileOutbound
import io.minio.MinioClient
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class UploadFileAdapter(private val minioClient: MinioClient): UploadFileOutbound {
    override fun uploadFile(
        inputStream: InputStream,
        fileName: String,
        contentType: String,
        bucketName: String
    ): String {
        return ""
    }
}