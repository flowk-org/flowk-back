package com.example.flowkback.app.impl.pipeline

import com.example.flowkback.adapter.docker.DockerAdapter
import com.example.flowkback.adapter.minio.MinioAdapter
import com.example.flowkback.app.api.docker.*
import com.example.flowkback.app.api.event.SaveEventOutbound
import com.example.flowkback.app.api.pipeline.ModelTrainingException
import com.example.flowkback.domain.project.Env
import com.example.flowkback.domain.project.StageConfig
import com.example.flowkback.utils.Directories.MODELS_DIR
import com.example.flowkback.utils.Directories.REPOS_DIR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.nio.file.Paths

@Component
class DeployModelDelegate(
    private val buildImageOutbound: BuildImageOutbound,
    private val createContainerOutbound: CreateContainerOutbound,
    private val dockerAdapter: DockerAdapter,
    private val generateDockerfileDelegate: GenerateDockerfileDelegate,
    private val streamLogsOutbound: StreamLogsOutbound
) {
    suspend fun deploy(
        projectName: String,
        trainConfig: StageConfig,
        deployConfig: StageConfig,
        env: Env,
    ) {
        val containerName = "deploy-$projectName-${System.currentTimeMillis()}"
        var containerId: String = ""

        try {
            val requirementsFile = "$REPOS_DIR/$projectName/${env.dependencies}"
            val servingScriptFile = "$REPOS_DIR/$projectName/${deployConfig.script}"
            val dockerfile = generateDockerfileDelegate.generate(
                pythonVersion = env.pyVersion,
                requirementsFile = requirementsFile,
                trainScriptFile = servingScriptFile,
                command = Paths.get(servingScriptFile).toFile().name
            )

            val imageId = buildImageOutbound.build(dockerfile, containerName)

            val models = "$MODELS_DIR/$projectName"
            containerId = createContainerOutbound.create(
                imageId,
                containerName,
                listOf(Mount(from = models, to = trainConfig.output?.path ?: "/models")),
                listOf(PortForwarding(hostPort = 8082, containerPort = 5000))
            )

            dockerAdapter.startContainer(containerId)

            val logs = StringBuilder()
            val logJob = CoroutineScope(Dispatchers.IO).launch {
                streamLogsOutbound.stream(containerId).collect { line ->
                    println("[DEPLOY LOG] $line")
                    logs.appendLine(line)
                }
            }

            val exitCode = withContext(Dispatchers.IO) {
                dockerAdapter.waitForContainer(containerId)
            }
//            logJob.join()

            if (exitCode != 0) {
                throw ModelTrainingException("Serving failed with exit code $exitCode. Logs: ${logs.take(500)}...")
            }

        } catch (e: Exception) {
            throw e
        }
    }
}