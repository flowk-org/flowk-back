package com.example.flowkback.app.impl.pipeline

import com.example.flowkback.adapter.docker.DockerAdapter
import com.example.flowkback.app.api.docker.BuildImageOutbound
import com.example.flowkback.app.api.docker.CreateContainerOutbound
import com.example.flowkback.app.api.docker.Mount
import com.example.flowkback.app.api.docker.StreamLogsOutbound
import com.example.flowkback.app.api.pipeline.ModelTrainingException
import com.example.flowkback.domain.project.Env
import com.example.flowkback.domain.project.StageConfig
import com.example.flowkback.utils.Directories
import com.example.flowkback.utils.Directories.METRICS_DIR
import com.example.flowkback.utils.Directories.MODELS_DIR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Paths

@Service
class EvaluateModelDelegate(
    private val buildImageOutbound: BuildImageOutbound,
    private val createContainerOutbound: CreateContainerOutbound,
    private val dockerAdapter: DockerAdapter,
    private val generateDockerfileDelegate: GenerateDockerfileDelegate,
    private val streamLogsOutbound: StreamLogsOutbound
) {
    /**
     * Оценка модели
     *
     * @param projectName название проекта
     * @param trainConfig конфигурация обучения
     * @param testConfig конфигурация оценки
     * @param env окружение этапа
     *
     * @return путь к обученной модели
     * @throws ModelTrainingException ошибка обучения модели
     */
    suspend fun evaluate(
        projectName: String,
        trainConfig: StageConfig,
        testConfig: StageConfig,
        env: Env,
    ): File {
        val containerName = "test-$projectName-${System.currentTimeMillis()}"
        var containerId: String = ""

        try {
            val requirementsFile = "${Directories.REPOS_DIR}/$projectName/${env.dependencies}"
            val testScriptFile = "${Directories.REPOS_DIR}/$projectName/${testConfig.script}"
            val dockerfile = generateDockerfileDelegate.generate(
                pythonVersion = env.pyVersion,
                requirementsFile = requirementsFile,
                trainScriptFile = testScriptFile,
                command = Paths.get(testScriptFile).toFile().name
            )

            val imageId = buildImageOutbound.build(dockerfile, containerName)

            val models = "$MODELS_DIR/$projectName"
            val metrics = "$METRICS_DIR/$projectName"
            containerId = createContainerOutbound.create(
                imageId,
                containerName,
                listOf(
                    Mount(from = models, to = trainConfig.output?.path ?: "/models"),
                    Mount(from = metrics, to = testConfig.output?.path ?: "/metrics")
                )
            )

            dockerAdapter.startContainer(containerId)

            val logs = StringBuilder()
            val logJob = CoroutineScope(Dispatchers.IO).launch {
                streamLogsOutbound.stream(containerId).collect { line ->
                    println("[TEST LOG] $line")
                    logs.appendLine(line)
                }
            }

            val exitCode = withContext(Dispatchers.IO) {
                dockerAdapter.waitForContainer(containerId)
            }
//            logJob.join()

            if (exitCode != 0) {
                throw ModelTrainingException("Testing failed with exit code $exitCode. Logs: ${logs.take(500)}...")
            }

            val metricsPath = "$METRICS_DIR/$projectName/${testConfig.output?.name}"
            return File(metricsPath).takeIf { it.exists() }
                ?: throw ModelTrainingException("Model file not found after training")

        } catch (e: Exception) {
            throw ModelTrainingException("Training failed with exception: $e")
        } finally {
            containerId.takeIf { it.isNotEmpty() }
                ?.let {
                    dockerAdapter.removeContainer(it)
                }
        }
    }
}