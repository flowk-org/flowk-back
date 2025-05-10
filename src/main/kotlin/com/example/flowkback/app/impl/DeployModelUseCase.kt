package com.example.flowkback.app.impl

import com.example.flowkback.adapter.docker.DockerAdapter
import com.example.flowkback.adapter.minio.MinioAdapter
import com.example.flowkback.app.api.SocketNotifier
import com.example.flowkback.app.api.pipeline.DeployModelInbound
import com.example.flowkback.domain.event.ModelDeploymentCompletedEvent
import com.example.flowkback.domain.event.ModelDeploymentFailedEvent
import com.example.flowkback.app.api.docker.*
import com.example.flowkback.app.api.event.SaveEventOutbound
import com.example.flowkback.app.api.pipeline.ModelTrainingException
import com.github.dockerjava.api.DockerClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Paths
import java.time.Instant

@Service
class DeployModelUseCase(
    private val buildImageOutbound: BuildImageOutbound,
    private val createContainerOutbound: CreateContainerOutbound,
    private val dockerAdapter: DockerAdapter,
    private val generateDockerfileDelegate: GenerateDockerfileDelegate,
    private val saveEventOutbound: SaveEventOutbound,
    private val socketNotifier: SocketNotifier,
    private val dockerClient: DockerClient,
    private val minioAdapter: MinioAdapter,
    private val streamLogsOutbound: StreamLogsOutbound
) : DeployModelInbound {
    override fun execute(
        servingScript: File,
        projectName: String,
        modelInputPath: String,
        pythonVersion: String
    ) {
        val containerName = "deploy-$projectName-${System.currentTimeMillis()}"
        var containerId: String = ""

        try {
            val fileName = "model.h5"
            val bucketName = "flowk-test"
            val modelFile = minioAdapter.downloadFile(
                fileName = fileName,
                bucketName = bucketName
            )
            FileUtils.copyInputStreamToFile(
                modelFile,
                File("./models/$projectName/model.h5")
            )

            val requirementsFile = "./repos/$projectName/requirements.txt"
            val servingScriptFile = "./repos/$projectName/serving.py"
            val dockerfile = generateDockerfileDelegate.generate(
                pythonVersion = pythonVersion,
                requirementsFile = requirementsFile,
                trainScriptFile = servingScriptFile,
                command = Paths.get(servingScriptFile).toFile().name
            )

            val imageId = buildImageOutbound.build(dockerfile, containerName)

            val models = "./models/$projectName"
            containerId = createContainerOutbound.create(
                imageId,
                containerName,
                listOf(Mount(from = models, to = modelInputPath)),
                listOf(PortForwarding(hostPort = 8082, containerPort = 5000))
            )

            dockerAdapter.startContainer(containerId)

            val logs = StringBuilder()
            CoroutineScope(Dispatchers.IO).launch {
                streamLogsOutbound.stream(containerId).collect { line ->
                    println("[DEPLOY LOG] $line")
                    logs.appendLine(line)
                }
            }

            val exitCode = dockerAdapter.waitForContainer(containerId)
            // добавить join() логов

            if (exitCode != 0) {
                throw ModelTrainingException("Serving failed with exit code $exitCode. Logs: ${logs.take(500)}...")
            }

            saveEventOutbound.save(
                ModelDeploymentCompletedEvent(
                    modelName = projectName,
                    logs = logs.toString(),
                    timestamp = Instant.now()
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
                ModelDeploymentFailedEvent(
                    modelName = projectName,
                    error = e.message ?: "Unknown error",
                    timestamp = Instant.now()
                )
            )
            // добавить кастомную ошибку
            throw e
        }
    }
}