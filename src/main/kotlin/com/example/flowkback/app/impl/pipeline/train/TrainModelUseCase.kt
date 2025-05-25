package com.example.flowkback.app.impl.pipeline.train

import com.example.flowkback.app.api.NotifyWebSocketOutbound
import com.example.flowkback.app.api.pipeline.TrainModelInbound
import com.example.flowkback.app.impl.pipeline.test.EvaluateModelUseCase
import com.example.flowkback.app.impl.event.store.EventStore
import com.example.flowkback.app.impl.model.registry.ModelRegistry
import com.example.flowkback.app.impl.project.UpdateRunUseCase
import com.example.flowkback.domain.event.train.ModelTrainedEvent
import com.example.flowkback.domain.event.train.ModelTrainingFailedEvent
import com.example.flowkback.domain.project.Config
import com.example.flowkback.domain.run.RunStatus
import com.example.flowkback.domain.run.StageType
import com.example.flowkback.utils.CoroutineUtils.catch
import com.example.flowkback.utils.CoroutineUtils.then
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TrainModelUseCase(
    private val trainModelDelegate: TrainModelDelegate,
    private val modelRegistry: ModelRegistry,
    private val eventStore: EventStore,
    private val notifyWebSocketOutbound: NotifyWebSocketOutbound,
    private val evaluateModelUseCase: EvaluateModelUseCase,
    private val updateRunUseCase: UpdateRunUseCase
) : TrainModelInbound {
    override fun execute(runId: String, projectName: String, projectConfig: Config) {
        updateRunUseCase.updateStatus(runId, StageType.TRAIN, RunStatus.RUNNING)

        CoroutineScope(Dispatchers.Default).async {
            trainModelDelegate.train(
                projectName,
                projectConfig.stages[1],
                projectConfig.env
            )
        }.then { model ->
            updateRunUseCase.updateStatus(runId, StageType.TRAIN, RunStatus.COMPLETED)
            val modelUrl = modelRegistry.saveModel(runId, projectName, model)

            val event = ModelTrainedEvent(
                runId = runId,
                modelName = projectName,
                modelUrl = modelUrl,
                trainedAt = Instant.now()
            )
            eventStore.save(event)
            notifyWebSocketOutbound.notify(event)

            evaluateModelUseCase.execute(runId, projectName, projectConfig)
        }.catch { exception ->
            updateRunUseCase.updateStatus(runId, StageType.TRAIN, RunStatus.FAILED)
            eventStore.save(
                ModelTrainingFailedEvent(
                    runId = runId,
                    modelName = projectName,
                    error = exception.message ?: "Unknown error",
                    timestamp = Instant.now()
                )
            )
        }
    }
}