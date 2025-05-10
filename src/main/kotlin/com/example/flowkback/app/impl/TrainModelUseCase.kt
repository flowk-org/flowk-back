package com.example.flowkback.app.impl

import com.example.flowkback.adapter.docker.DockerAdapter
import com.example.flowkback.app.api.*
import com.example.flowkback.app.api.docker.BuildImageOutbound
import com.example.flowkback.app.api.docker.CreateContainerOutbound
import com.example.flowkback.app.api.event.SaveEventOutbound
import com.example.flowkback.app.api.docker.Mount
import com.example.flowkback.app.api.docker.StreamLogsOutbound
import com.example.flowkback.app.api.pipeline.TrainModelInbound
import com.example.flowkback.domain.event.ModelTrainedEvent
import com.example.flowkback.domain.event.ModelTrainingFailedEvent
import com.example.flowkback.app.api.pipeline.ModelTrainingException
import com.example.flowkback.app.api.pipeline.TrainingCompleteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Paths
import java.time.Instant

@Service
class TrainModelUseCase(
    private val buildImageOutbound: BuildImageOutbound,
    private val createContainerOutbound: CreateContainerOutbound,
    private val dockerAdapter: DockerAdapter,
    private val generateDockerfileDelegate: GenerateDockerfileDelegate,
    private val uploadFileOutbound: UploadFileOutbound,
    private val saveEventOutbound: SaveEventOutbound,
    private val socketNotifier: SocketNotifier,
    private val streamLogsOutbound: StreamLogsOutbound
) : TrainModelInbound {
    override fun execute(
        trainScript: File,
        projectName: String,
        modelOutputPath: String,
        pythonVersion: String
    ) {
        val containerName = "train-$projectName-${System.currentTimeMillis()}"
        var containerId: String = ""

        try {
            val requirementsFile = "./repos/$projectName/requirements.txt"
            val trainScriptFile = "./repos/$projectName/train.py"
            val dockerfile = generateDockerfileDelegate.generate(
                pythonVersion = pythonVersion,
//                outputDirectory = "./dockerfiles/$projectName",
                requirementsFile = requirementsFile,
                trainScriptFile = trainScriptFile,
                command = Paths.get(trainScriptFile).toFile().name
            )

            val imageId = buildImageOutbound.build(dockerfile, containerName)

            val from = "./models/$projectName"
            containerId = createContainerOutbound.create(
                imageId,
                containerName,
                listOf(Mount(from = from, to = modelOutputPath))
            )

            dockerAdapter.startContainer(containerId)

            val logs = StringBuilder()
           CoroutineScope(Dispatchers.IO).launch {
                streamLogsOutbound.stream(containerId).collect { line ->
                    println("[TRAIN LOG] $line")
                    logs.appendLine(line)
                }
            }

            val exitCode = dockerAdapter.waitForContainer(containerId)
            // добавить join() логов

            if (exitCode != 0) {
                throw ModelTrainingException("Training failed with exit code $exitCode. Logs: ${logs.take(500)}...")
            }

            val modelPath = "models/$projectName/model.h5"
            val modelFile = File(modelPath).takeIf { it.exists() }
                ?: throw ModelTrainingException("Model file not found after training")

            val modelVersion = "v1.0.0"
            val modelUrl = uploadFileOutbound.upload(
                inputStream = modelFile.inputStream(),
                fileName = modelFile.name,
                contentType = "application/octet-stream",
                bucketName = projectName,
                tags = mapOf(
                    "project" to projectName,
                    "version" to modelVersion,
                    "stage" to "train"
                )
            )

            saveEventOutbound.save(
                ModelTrainedEvent(
                    modelName = projectName,
                    modelUrl = modelUrl,
                    logs = logs.toString(),
                    trainedAt = Instant.now()
                )
            )

            // поправить сокет
            socketNotifier.notifyTrainingComplete(
                TrainingCompleteMessage(
                    modelName = projectName,
                    status = "SUCCESS",
                    modelUrl = modelUrl
                )
            )
        } catch (e: Exception) {
            saveEventOutbound.save(
                ModelTrainingFailedEvent(
                    modelName = projectName,
                    error = e.message ?: "Unknown error",
                    timestamp = Instant.now()
                )
            )
            // добавить кастомную ошибку
            throw e
        } finally {
            containerId.takeIf { it.isNotEmpty() }
                ?.let {
                    dockerAdapter.removeContainer(it)
                }
        }
    }
}