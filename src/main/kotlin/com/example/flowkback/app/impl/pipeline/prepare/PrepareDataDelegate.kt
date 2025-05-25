package com.example.flowkback.app.impl.pipeline.prepare

import com.example.flowkback.adapter.docker.RemoveContainerAdapter
import com.example.flowkback.app.api.docker.*
import com.example.flowkback.app.api.pipeline.DataPreparationException
import com.example.flowkback.app.impl.pipeline.GenerateDockerfileDelegate
import com.example.flowkback.domain.project.Env
import com.example.flowkback.domain.project.StageConfig
import com.example.flowkback.utils.Directories.REPOS_DIR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class PrepareDataDelegate(
    private val generateDockerfileDelegate: GenerateDockerfileDelegate,
    private val buildImageOutbound: BuildImageOutbound,
    private val startContainerOutbound: StartContainerOutbound,
    private val createContainerOutbound: CreateContainerOutbound,
    private val removeContainerAdapter: RemoveContainerAdapter,
    private val streamLogsOutbound: StreamLogsOutbound,
    private val waitForContainerOutbound: WaitForContainerOutbound
) {
    /**
     * Подготовить данные
     *
     * @param projectName название проекта
     * @param prepareConfig конфигурация этапа
     * @param env окружение этапа
     *
     * @throws DataPreparationException ошибка подготовки данных
     */
    suspend fun prepare(
        projectName: String,
        prepareConfig: StageConfig,
        env: Env,
    ) {
        val containerName = "prepare-$projectName-${System.currentTimeMillis()}"
        var containerId: String = ""

        try {
            val migrationsDir = "${prepareConfig.output?.path}/${prepareConfig.output?.name}"
            val dir = "$REPOS_DIR/$projectName${prepareConfig.output?.path}"
            val dockerfile = generateDockerfileDelegate.generate(
                dir = dir,
                migrationsDir = migrationsDir,
                scriptFile = "./src/main/resources/db/clickhouse/migrate.sh"
            )

            val imageId = buildImageOutbound.build(dockerfile, containerName)
            containerId = createContainerOutbound.create(
                image = imageId,
                containerName = containerName,
                network = "flowk-back_default"
            )

            startContainerOutbound.start(containerId)

            val logs = StringBuilder()
            val logJob = CoroutineScope(Dispatchers.IO).launch {
                streamLogsOutbound.stream(containerId).collect { line ->
                    println("[DATA PREP LOG] $line")
                    logs.appendLine(line)
                }
            }

            val exitCode = withContext(Dispatchers.IO) {
                waitForContainerOutbound.wait(containerId)
            }
//            logJob.join()

            if (exitCode != 0) {
                throw DataPreparationException("Prep failed with exit code $exitCode. Logs: ${logs.take(500)}...")
            }

        } catch (e: Exception) {
            throw DataPreparationException("Data preparation failed with exception: $e")
        } finally {
            containerId.takeIf { it.isNotEmpty() }
                ?.let {
                    removeContainerAdapter.remove(it)
                }
        }
    }
}