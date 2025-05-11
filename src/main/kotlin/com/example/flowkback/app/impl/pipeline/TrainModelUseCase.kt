package com.example.flowkback.app.impl.pipeline

import com.example.flowkback.app.api.pipeline.TrainModelInbound
import com.example.flowkback.domain.event.train.ModelTrainedEvent
import com.example.flowkback.domain.event.train.ModelTrainingFailedEvent
import com.example.flowkback.domain.project.Config
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
    private val evaluateModelUseCase: EvaluateModelUseCase
) : TrainModelInbound {
    override fun execute(projectName: String, projectConfig: Config) {
        CoroutineScope(Dispatchers.Default).async {
            trainModelDelegate.train(
                projectName,
                projectConfig.stages[1],
                projectConfig.env
            )
        }.then { model ->
            val modelUrl = modelRegistry.saveModel(projectName, model)

            eventStore.save(
                ModelTrainedEvent(
                    modelName = projectName,
                    modelUrl = modelUrl,
                    trainedAt = Instant.now()
                )
            )

            evaluateModelUseCase.execute(projectName, projectConfig)
        }.catch { exception ->
            eventStore.save(
                ModelTrainingFailedEvent(
                    modelName = projectName,
                    error = exception.message ?: "Unknown error",
                    timestamp = Instant.now()
                )
            )
        }
    }
}