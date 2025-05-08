package com.example.flowkback.app.impl

import com.example.flowkback.adapter.docker.DockerAdapter
import com.example.flowkback.adapter.minio.MinioAdapter
import com.example.flowkback.app.api.*
import com.example.flowkback.app.api.docker.BuildImageOutbound
import com.example.flowkback.app.api.docker.CreateContainerOutbound
import com.example.flowkback.app.api.event.SaveEventOutbound
import com.example.flowkback.app.api.docker.Mount
import com.example.flowkback.app.api.docker.StreamLogsOutbound
import com.example.flowkback.app.api.test.EvaluateModelInbound
import com.example.flowkback.app.api.test.ModelEvaluationCompletedEvent
import com.example.flowkback.app.api.test.ModelEvaluationFailedEvent
import com.example.flowkback.app.api.train.ModelTrainingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Paths
import java.time.Instant
import kotlin.concurrent.thread

@Service
class EvaluateModelUseCase(
    private val buildImageOutbound: BuildImageOutbound,
    private val createContainerOutbound: CreateContainerOutbound,
    private val dockerAdapter: DockerAdapter,
    private val generateDockerfileDelegate: GenerateDockerfileDelegate,
    private val saveEventOutbound: SaveEventOutbound,
    private val socketNotifier: SocketNotifier,
    private val minioAdapter: MinioAdapter,
    private val streamLogsOutbound: StreamLogsOutbound
) : EvaluateModelInbound {
    override fun execute(
        testScript: File,
        projectName: String,
        modelInputPath: String,
        metricsOutputPath: String,
        pythonVersion: String
    ) {
        val containerName = "test-$projectName-${System.currentTimeMillis()}"
        var containerId: String = ""

        try {
            val fileName = "model.h5"
            val bucketName = "flowk-test"
            val modelFile = minioAdapter.downloadFile(
                fileName = fileName,
                bucketName = bucketName
            )
            FileUtils.copyInputStreamToFile(modelFile,
                File("./models/$projectName/model.h5")
            )

            val requirementsFile = "./repos/$projectName/requirements.txt"
            val testScriptFile = "./repos/$projectName/test.py"
            val dockerfile = generateDockerfileDelegate.generate(
                pythonVersion = pythonVersion,
                requirementsFile = requirementsFile,
                trainScriptFile = testScriptFile,
                command = Paths.get(testScriptFile).toFile().name
            )

            val imageId = buildImageOutbound.build(dockerfile, containerName)

            val models = "./models/$projectName"
            val metrics = "./metrics/$projectName"
            containerId = createContainerOutbound.create(
                imageId,
                containerName,
                listOf(
                    Mount(from = models, to = modelInputPath),
                    Mount(from = metrics, to = metricsOutputPath)
                )
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

            if (exitCode != 0) {
                throw ModelTrainingException("Testing failed with exit code $exitCode. Logs: ${logs.take(500)}...")
            }

            val metricsPath = "metrics/$projectName/metrics.json"
            val metricsFile = File(metricsPath).takeIf { it.exists() }
                ?: throw ModelTrainingException("Model file not found after training")

//            dockerAdapter.removeContainer(containerId)

            saveEventOutbound.save(
                ModelEvaluationCompletedEvent(
                    modelName = projectName,
                    metrics = FileUtils.readFileToString(metricsFile),
                    logs = logs.toString(),
                    evaluatedAt = Instant.now()
                )
            )

//            socketNotifier.notifyTrainingComplete(
//                TrainingCompleteMessage(
//                    modelName = projectName,
//                    status = "SUCCESS",
//                    modelUrl = modelUrl
//                )
//            )
        } catch (e: Exception) {
            saveEventOutbound.save(
                ModelEvaluationFailedEvent(
                    modelName = projectName,
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