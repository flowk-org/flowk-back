package com.example.flowkback.app.impl

import com.example.flowkback.adapter.docker.DockerAdapter
import com.example.flowkback.app.api.*
import com.example.flowkback.app.api.docker.BuildImageOutbound
import com.example.flowkback.app.api.docker.CreateContainerOutbound
import com.example.flowkback.app.api.event.SaveEventOutbound
import com.example.flowkback.app.api.docker.Mount
import com.example.flowkback.app.api.train.TrainModelInbound
import com.example.flowkback.app.api.event.ModelTrainedEvent
import com.example.flowkback.app.api.event.ModelTrainingFailedEvent
import com.example.flowkback.app.api.train.ModelTrainingException
import com.example.flowkback.app.api.train.TrainingCompleteMessage
import org.springframework.stereotype.Service
import java.io.File
import java.time.Instant
import kotlin.concurrent.thread

private const val s = "data/requirements.txt"

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
        val containerName = "train-$modelName-${System.currentTimeMillis()}"
        var containerId: String = ""

        try {
            val requirementsFile = "data/requirements.txt"
            val pythonVersion = "3.10"
            val dockerfile = generateDockerfileDelegate.generate(
                pythonVersion = pythonVersion,
                requirementsFile = requirementsFile,
                command = trainScript.name
            )

            val imageId = buildImageOutbound.build(dockerfile, containerName)

            val from = "/data/models"
            val to = "/app/models"
            containerId = createContainerOutbound.create(
                imageId,
                containerName,
                listOf(Mount(from = from, to = to))
            )

            dockerAdapter.startContainer(containerId)

            val logs = StringBuilder()
            val logThread = thread {
                dockerAdapter.getContainerLogs(containerId).lines().forEach { line ->
                    logs.appendLine(line)
                    println("[TRAIN LOG] $line")
                }
            }

            val exitCode = dockerAdapter.waitForContainer(containerId)
            logThread.join()

            if (exitCode != 0) {
                throw ModelTrainingException("Training failed with exit code $exitCode. Logs: ${logs.take(500)}...")
            }

            val modelPath = "data/models/model.h5"
            val modelFile = File(modelPath).takeIf { it.exists() }
                ?: throw ModelTrainingException("Model file not found after training")

//            dockerAdapter.removeContainer(containerId)

            val bucketName = "models"
            val modelUrl = uploadFileOutbound.upload(
                inputStream = modelFile.inputStream(),
                fileName = modelFile.name,
                contentType = "application/octet-stream",
                bucketName = bucketName
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
            saveEventOutbound.save(
                ModelTrainingFailedEvent(
                    modelName = modelName,
                    error = e.message ?: "Unknown error",
                    timestamp = Instant.now()
                )
            )
            throw e
        } finally {
            containerId.takeIf { it.isNotEmpty() }
                ?.let {
                    dockerAdapter.removeContainer(it)
                }
        }
    }
}