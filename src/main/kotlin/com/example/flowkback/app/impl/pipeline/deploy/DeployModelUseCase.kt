package com.example.flowkback.app.impl.pipeline.deploy

import com.example.flowkback.adapter.minio.MinioAdapter
import com.example.flowkback.app.api.pipeline.DeployModelInbound
import com.example.flowkback.domain.event.deploy.ModelDeploymentCompletedEvent
import com.example.flowkback.domain.event.deploy.ModelDeploymentFailedEvent
import com.example.flowkback.app.api.event.SaveEventOutbound
import com.example.flowkback.app.impl.project.UpdateRunUseCase
import com.example.flowkback.domain.project.Config
import com.example.flowkback.domain.run.RunStatus
import com.example.flowkback.domain.run.StageType
import com.example.flowkback.utils.ConfigParser
import com.example.flowkback.utils.CoroutineUtils.catch
import com.example.flowkback.utils.CoroutineUtils.then
import com.example.flowkback.utils.Directories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.apache.commons.io.FileUtils
import org.springframework.stereotype.Service
import java.io.File
import java.time.Instant

@Service
class DeployModelUseCase(
    private val deployModelDelegate: DeployModelDelegate,
    private val saveEventOutbound: SaveEventOutbound,
    private val minioAdapter: MinioAdapter,
    private val updateRunUseCase: UpdateRunUseCase
) : DeployModelInbound {
    override fun execute(runId: String, projectName: String, projectConfig: Config) {
        updateRunUseCase.updateStatus(runId, StageType.DEPLOY, RunStatus.RUNNING)

        val trainConfig = ConfigParser.parseFromFile("./repos/flowk-test/mlci.yaml").stages[1]
        // заменить на modelRegistry.getLatestModelForProject()
        // или modelRegistry.getModelByProjectAndVersion
        val modelFile = minioAdapter.downloadFile(
            fileName = trainConfig.output?.name ?: "/models",
            bucketName = projectName
        )

        FileUtils.copyInputStreamToFile(
            modelFile,
            File("${Directories.MODELS_DIR}/$projectName/${trainConfig.output?.name}")
        )

        CoroutineScope(Dispatchers.Default).async {
            deployModelDelegate.deploy(
                projectName,
                projectConfig.stages[1],
                projectConfig.stages[3],
                projectConfig.env
            )
        }.then {
            updateRunUseCase.updateStatus(runId, StageType.DEPLOY, RunStatus.COMPLETED)

            saveEventOutbound.save(
                ModelDeploymentCompletedEvent(
                    runId = runId,
                    modelName = projectName,
                    timestamp = Instant.now()
                )
            )
        }.catch { exception ->
            updateRunUseCase.updateStatus(runId, StageType.DEPLOY, RunStatus.FAILED)
            saveEventOutbound.save(
                ModelDeploymentFailedEvent(
                    runId = runId,
                    modelName = projectName,
                    error = exception.message ?: "Unknown error",
                    timestamp = Instant.now()
                )
            )
        }
    }
}