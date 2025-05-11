package com.example.flowkback.app.impl.pipeline

import com.example.flowkback.adapter.minio.MinioAdapter
import com.example.flowkback.app.api.pipeline.EvaluateModelInbound
import com.example.flowkback.domain.project.Config
import com.example.flowkback.domain.event.test.ModelEvaluationCompletedEvent
import com.example.flowkback.domain.event.test.ModelEvaluationFailedEvent
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
    private val deployModelUseCase: DeployModelUseCase
) : EvaluateModelInbound {
    override fun execute(projectName: String, projectConfig: Config) {
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
            eventStore.save(
                ModelEvaluationCompletedEvent(
                    modelName = projectName,
                    metrics = FileUtils.readFileToString(metrics),
                    evaluatedAt = Instant.now()
                )
            )

            deployModelUseCase.execute(projectName, projectConfig)
        }.catch { exception ->
            eventStore.save(
                ModelEvaluationFailedEvent(
                    modelName = projectName,
                    error = exception.message ?: "Unknown error",
                    timestamp = Instant.now()
                )
            )
        }
    }
}