package com.example.flowkback.app.impl.pipeline.test

import com.example.flowkback.adapter.minio.MinioAdapter
import com.example.flowkback.app.api.NotifyWebSocketOutbound
import com.example.flowkback.app.api.pipeline.EvaluateModelInbound
import com.example.flowkback.app.impl.event.store.EventStore
import com.example.flowkback.app.impl.pipeline.deploy.DeployModelUseCase
import com.example.flowkback.app.impl.project.UpdateRunUseCase
import com.example.flowkback.domain.project.Config
import com.example.flowkback.domain.event.test.ModelEvaluationCompletedEvent
import com.example.flowkback.domain.event.test.ModelEvaluationFailedEvent
import com.example.flowkback.domain.run.RunStatus
import com.example.flowkback.domain.run.StageType
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
class EvaluateModelUseCase(
    private val eventStore: EventStore,
    private val minioAdapter: MinioAdapter,
    private val evaluateModelDelegate: EvaluateModelDelegate,
    private val notifyWebSocketOutbound: NotifyWebSocketOutbound,
    private val deployModelUseCase: DeployModelUseCase,
    private val updateRunUseCase: UpdateRunUseCase
) : EvaluateModelInbound {
    override fun execute(runId: String, projectName: String, projectConfig: Config) {
        updateRunUseCase.updateStatus(runId, StageType.TEST, RunStatus.RUNNING)

        val trainConfig = projectConfig.stages[1]
        val testConfig = projectConfig.stages[2]

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
            evaluateModelDelegate.evaluate(
                projectName = projectName,
                trainConfig = trainConfig,
                testConfig = testConfig,
                env = projectConfig.env
            )
        }.then { metrics ->
            // добавить modelRegistry.addMetrics()

            updateRunUseCase.updateStatus(runId, StageType.TEST, RunStatus.COMPLETED)
            val event = ModelEvaluationCompletedEvent(
                runId = runId,
                modelName = projectName,
                metrics = FileUtils.readFileToString(metrics),
                evaluatedAt = Instant.now()
            )
            eventStore.save(event)
            notifyWebSocketOutbound.notify(event)

            deployModelUseCase.execute(runId, projectName, projectConfig)
        }.catch { exception ->
            updateRunUseCase.updateStatus(runId, StageType.TEST, RunStatus.FAILED)
            eventStore.save(
                ModelEvaluationFailedEvent(
                    runId = runId,
                    modelName = projectName,
                    error = exception.message ?: "Unknown error",
                    timestamp = Instant.now()
                )
            )
        }
    }
}