package com.example.flowkback.app.impl.pipeline.train

import com.example.flowkback.adapter.docker.RemoveContainerAdapter
import com.example.flowkback.app.api.docker.*
import com.example.flowkback.app.api.pipeline.ModelTrainingException
import com.example.flowkback.app.impl.pipeline.GenerateDockerfileDelegate
import com.example.flowkback.domain.project.Env
import com.example.flowkback.domain.project.StageConfig
import com.example.flowkback.utils.Directories.MODELS_DIR
import com.example.flowkback.utils.Directories.REPOS_DIR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Paths

@Component
class TrainModelDelegate (
    private val buildImageOutbound: BuildImageOutbound,
    private val createContainerOutbound: CreateContainerOutbound,
    private val removeContainerAdapter: RemoveContainerAdapter,
    private val startContainerOutbound: StartContainerOutbound,
    private val generateDockerfileDelegate: GenerateDockerfileDelegate,
    private val streamLogsOutbound: StreamLogsOutbound,
    private val waitForContainerOutbound: WaitForContainerOutbound
) {
    /**
     * Обучить модель
     *
     * @param projectName название проекта
     * @param trainConfig конфигурация обучения
     * @param env окружение этапа
     *
     * @return путь к обученной модели
     * @throws ModelTrainingException ошибка обучения модели
     */
    suspend fun train(
        projectName: String,
        trainConfig: StageConfig,
        env: Env,
    ): File {
        val containerName = "train-$projectName-${System.currentTimeMillis()}"
        var containerId: String = ""

        try {
            val requirementsFile = "$REPOS_DIR/$projectName/${env.dependencies}"
            val trainScriptFile = "$REPOS_DIR/$projectName/${trainConfig.script}"
            val dockerfile = generateDockerfileDelegate.generate(
                pythonVersion = env.pyVersion,
                requirementsFile = requirementsFile,
                trainScriptFile = trainScriptFile,
                command = Paths.get(trainScriptFile).toFile().name
            )

            val imageId = buildImageOutbound.build(dockerfile, containerName)

            val from = "$MODELS_DIR/$projectName"
            containerId = createContainerOutbound.create(
                imageId,
                containerName,
                listOf(Mount(from = from, to = trainConfig.output?.path ?: "/models")),
                network = "flowk-back_default"
            )

            startContainerOutbound.start(containerId)

            val logs = StringBuilder()
            val logJob = CoroutineScope(Dispatchers.IO).launch {
                streamLogsOutbound.stream(containerId).collect { line ->
                    println("[TRAIN LOG] $line")
                    logs.appendLine(line)
                }
            }

            val exitCode = withContext(Dispatchers.IO) {
                waitForContainerOutbound.wait(containerId)
            }
//            logJob.join()

            if (exitCode != 0) {
                throw ModelTrainingException("Training failed with exit code $exitCode. Logs: ${logs.take(500)}...")
            }

            val modelPath = "$MODELS_DIR/$projectName/${trainConfig.output?.name}"
            return File(modelPath).takeIf { it.exists() }
                ?: throw ModelTrainingException("Model file not found after training")

        } catch (e: Exception) {
            throw ModelTrainingException("Training failed with exception: $e")
        } finally {
            containerId.takeIf { it.isNotEmpty() }
                ?.let {
                    removeContainerAdapter.remove(it)
                }
        }
    }
}