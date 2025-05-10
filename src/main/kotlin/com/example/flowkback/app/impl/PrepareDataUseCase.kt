package com.example.flowkback.app.impl

import com.example.flowkback.adapter.docker.DockerAdapter
import com.example.flowkback.app.api.SocketNotifier
import com.example.flowkback.app.api.docker.*
import com.example.flowkback.app.api.event.SaveEventOutbound
import com.example.flowkback.app.api.pipeline.DataPreparationException
import com.example.flowkback.app.api.pipeline.ModelTrainingException
import com.example.flowkback.app.api.pipeline.PrepareDataInbound
import com.example.flowkback.app.api.pipeline.TrainingCompleteMessage
import com.example.flowkback.domain.event.DataPreparedEvent
import com.example.flowkback.domain.event.DataPreparationFailedEvent
import com.example.flowkback.domain.event.ModelTrainedEvent
import com.example.flowkback.domain.event.ModelTrainingFailedEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Paths
import java.time.Instant

@Service
class PrepareDataUseCase(
    private val generateDockerfileDelegate: GenerateDockerfileDelegate,
    private val buildImageOutbound: BuildImageOutbound,
    private val createContainerOutbound: CreateContainerOutbound,
    private val dockerAdapter: DockerAdapter,
    private val streamLogsOutbound: StreamLogsOutbound,
    private val saveEventOutbound: SaveEventOutbound,
    private val runWebSocketService: RunWebSocketService,
    private val socketNotifier: SocketNotifier
) : PrepareDataInbound {

    override fun execute(projectName: String, migrationsPath: String) {
        val containerName = "prepare-$projectName-${System.currentTimeMillis()}"
        var containerId: String = ""

        try {
            val migrationsDir = "/db/migrations"
            val dir = "./repos/$projectName/db"
            val dockerfile = generateDockerfileDelegate.generate(
                dir = dir,
                migrationsDir = migrationsDir,
                scriptFile = "./src/main/resources/db/clickhouse/migrate.sh"
            )

            val imageId = buildImageOutbound.build(dockerfile, containerName)
            containerId = createContainerOutbound.create(
                image = imageId,
                containerName = containerName,
//                mounts = listOf(Mount(from = dir, to = "/app")),
                network = "flowk-back_default"
            )

            dockerAdapter.startContainer(containerId)

            val logs = StringBuilder()
            CoroutineScope(Dispatchers.IO).launch {
                streamLogsOutbound.stream(containerId).collect { line ->
                    println("[PREPARATION LOG] $line")
                    logs.appendLine(line)
                }
            }

            val exitCode = dockerAdapter.waitForContainer(containerId)
            // добавить join() логов

            if (exitCode != 0) {
                throw DataPreparationException("Prep failed with exit code $exitCode. Logs: ${logs.take(500)}...")
            }

            saveEventOutbound.save(
                DataPreparedEvent(
                    status = "SUCCESS",
                    logs = logs.toString(),
                    timestamp = Instant.now()
                )
            )
            runWebSocketService.sendRunUpdate()
        } catch (e: Exception) {
            saveEventOutbound.save(
                DataPreparationFailedEvent(
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
