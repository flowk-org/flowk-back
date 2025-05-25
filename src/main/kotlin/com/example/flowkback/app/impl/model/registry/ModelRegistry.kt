package com.example.flowkback.app.impl.model.registry

import com.example.flowkback.adapter.minio.MinioAdapter
import com.example.flowkback.adapter.mongo.model.ModelRepository
import com.example.flowkback.app.api.UploadFileOutbound
import com.example.flowkback.domain.model.ModelArtifact
import org.springframework.stereotype.Component
import java.io.File

@Component
class ModelRegistry(
    private val minioAdapter: MinioAdapter,
    private val uploadFileOutbound: UploadFileOutbound,
    private val modelRepository: ModelRepository
) {
    fun saveModel(runId: String, projectName: String, model: File): String {
        val modelVersion = "v1.0.0"
        val url = uploadFileOutbound.upload(
            inputStream = model.inputStream(),
            fileName = model.name,
            bucketName = projectName,
            tags = mapOf(
                "project" to projectName,
                "version" to modelVersion,
            )
        )

        return modelRepository.save(
            ModelArtifact(
                projectId = projectName,
                runId = runId,
                version = modelVersion,
                path = url
            )
        ).path
    }

//    fun getModel() {
//        minioAdapter.downloadFile()
//    }
}