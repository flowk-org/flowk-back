package com.example.flowkback.app.impl

import com.example.flowkback.adapter.docker.DockerAdapter
import com.example.flowkback.adapter.minio.MinioAdapter
import com.example.flowkback.app.api.*
import com.example.flowkback.domain.event.ModelTrainedEvent
import com.github.dockerjava.api.DockerClient
import org.springframework.stereotype.Service
import java.io.File
import java.time.Instant
import kotlin.concurrent.thread

@Service
class TrainModelUseCase(
    private val buildImageOutbound: BuildImageOutbound,
    private val createContainerOutbound: CreateContainerOutbound,
    private val dockerAdapter: DockerAdapter,
    private val generateDockerfileDelegate: GenerateDockerfileDelegate,
    private val uploadFileOutbound: UploadFileOutbound,
    private val saveEventOutbound: SaveEventOutbound,
    private val socketNotifier: SocketNotifier
) : TrainModelInbound {
    override fun execute(trainScript: File, modelName: String) {
        try {
            // 1. Генерация Dockerfile
            val dockerfile = generateDockerfileDelegate.generate(
                pythonVersion = "3.10",
                requirementsFile = "data/requirements.txt",
                command = trainScript.name
            )

            val containerName = "train-$modelName-${System.currentTimeMillis()}"
            val imageId = buildImageOutbound.build(dockerfile, containerName)

            val containerId = createContainerOutbound.create(
                imageId,
                containerName,
                listOf(Mount(from = "/data/models", to = "/app/models"))
            )

            dockerAdapter.startContainer(containerId)

            // 5. Мониторинг логов в реальном времени
            val logs = StringBuilder()
            val logThread = thread {
                dockerAdapter.getContainerLogs(containerId).lines().forEach { line ->
                    logs.appendLine(line)
                    println("[TRAIN LOG] $line")
                }
            }

            // 6. Ожидание завершения
            val exitCode = dockerAdapter.waitForContainer(containerId)
            logThread.join()

            if (exitCode != 0) {
                throw ModelTrainingException("Training failed with exit code $exitCode. Logs: ${logs.take(500)}...")
            }

            val modelFile = File("data/models/model.h5").takeIf { it.exists() }
                ?: throw ModelTrainingException("Model file not found after training")

            dockerAdapter.removeContainer(containerId)

            val modelUrl = uploadFileOutbound.upload(
                inputStream = modelFile.inputStream(),
                fileName = modelFile.name,
                contentType = "application/octet-stream",
                bucketName = "models"
            )

            saveEventOutbound.save(
                ModelTrainedEvent(
                    modelName = modelName,
                    modelUrl = modelUrl,
                    logs = logs.toString(),
                    trainedAt = Instant.now()
                )
            )

            socketNotifier.notifyTrainingComplete(
                TrainingCompleteMessage(
                    modelName = modelName,
                    status = "SUCCESS",
                    modelUrl = modelUrl
                )
            )
        } catch (e: Exception) {
//            eventStore.save(
//                ModelTrainingFailedEvent(
//                    modelName = modelName,
//                    error = e.message ?: "Unknown error",
//                    timestamp = Instant.now()
//                )
//            )
            throw e
        } finally {
            // Гарантированная очистка контейнера
//            dockerAdapter.removeContainer(containerId)
        }
    }

    // Вспомогательные классы
    class ModelTrainingException(message: String) : RuntimeException(message)

    data class TrainingCompleteMessage(
        val modelName: String,
        val status: String,
        val modelUrl: String
    )
}