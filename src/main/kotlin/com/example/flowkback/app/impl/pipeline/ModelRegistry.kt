package com.example.flowkback.app.impl.pipeline

import com.example.flowkback.adapter.minio.MinioAdapter
import com.example.flowkback.adapter.minio.UploadFileAdapter
import com.example.flowkback.adapter.mongo.model.ModelRepository
import com.example.flowkback.app.api.UploadFileOutbound
import org.springframework.stereotype.Component
import java.io.File

@Component
class ModelRegistry(
    private val minioAdapter: MinioAdapter,
    private val uploadFileOutbound: UploadFileOutbound,
    private val modelRepository: ModelRepository
) {
    fun saveModel(projectName: String, model: File): String {
        val modelVersion = "v1.0.0"
        return uploadFileOutbound.upload(
            inputStream = model.inputStream(),
            fileName = model.name,
            bucketName = projectName,
            tags = mapOf(
                "project" to projectName,
                "version" to modelVersion,
            )
        )
    }

//    fun getModel() {
//        minioAdapter.downloadFile()
//    }
}