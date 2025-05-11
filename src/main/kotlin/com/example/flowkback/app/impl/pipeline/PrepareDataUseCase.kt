package com.example.flowkback.app.impl.pipeline

import com.example.flowkback.adapter.rest.run.RunDto
import com.example.flowkback.app.api.NotifyWebSocketOutbound
import com.example.flowkback.app.api.pipeline.PrepareDataInbound
import com.example.flowkback.domain.project.Config
import com.example.flowkback.domain.event.prep.DataPreparedEvent
import com.example.flowkback.domain.event.prep.DataPreparationFailedEvent
import com.example.flowkback.utils.CoroutineUtils.catch
import com.example.flowkback.utils.CoroutineUtils.then
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class PrepareDataUseCase(
    private val eventStore: EventStore,
    private val notifyWebSocketOutbound: NotifyWebSocketOutbound,
    private val prepareDataDelegate: PrepareDataDelegate,
    private val trainModelUseCase: TrainModelUseCase
) : PrepareDataInbound {

    override fun execute(projectName: String, projectConfig: Config) {
        CoroutineScope(Dispatchers.Default).async {
            prepareDataDelegate.prepare(
                projectName,
                projectConfig.stages[0],
                projectConfig.env
            )
        }.then {
            val event = DataPreparedEvent(
                modelName = projectName,
                status = "SUCCESS",
                timestamp = Instant.now()
            )

            eventStore.save(event)
            notifyWebSocketOutbound.notify(event)

            trainModelUseCase.execute(projectName, projectConfig)
        }.catch { exception ->
            eventStore.save(
                DataPreparationFailedEvent(
                    modelName = projectName,
                    error = exception.message ?: "Unknown error",
                    timestamp = Instant.now()
                )
            )
        }
    }
}