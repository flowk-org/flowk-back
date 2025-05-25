package com.example.flowkback.app.impl.pipeline.prepare

import com.example.flowkback.adapter.mongo.run.RunRepository
import com.example.flowkback.app.api.NotifyWebSocketOutbound
import com.example.flowkback.app.api.pipeline.PrepareDataInbound
import com.example.flowkback.app.impl.event.store.EventStore
import com.example.flowkback.app.impl.pipeline.train.TrainModelUseCase
import com.example.flowkback.app.impl.project.UpdateRunUseCase
import com.example.flowkback.domain.project.Config
import com.example.flowkback.domain.event.prep.DataPreparedEvent
import com.example.flowkback.domain.event.prep.DataPreparationFailedEvent
import com.example.flowkback.domain.run.RunStatus
import com.example.flowkback.domain.run.StageType
import com.example.flowkback.utils.CoroutineUtils.catch
import com.example.flowkback.utils.CoroutineUtils.then
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime

@Service
class PrepareDataUseCase(
    private val eventStore: EventStore,
    private val notifyWebSocketOutbound: NotifyWebSocketOutbound,
    private val prepareDataDelegate: PrepareDataDelegate,
    private val trainModelUseCase: TrainModelUseCase,
    private val updateRunUseCase: UpdateRunUseCase,
    private val runRepository: RunRepository
) : PrepareDataInbound {

    override fun execute(
        runId: String,
        projectName: String,
        projectConfig: Config
    ) {
        updateRunUseCase.updateStatus(runId, StageType.PREPARE, RunStatus.RUNNING)

        CoroutineScope(Dispatchers.Default).async {
            prepareDataDelegate.prepare(
                projectName,
                projectConfig.stages[0],
                projectConfig.env
            )
        }.then {
            updateRunUseCase.updateStatus(runId, StageType.PREPARE, RunStatus.COMPLETED)

            val event = DataPreparedEvent(
                runId = runId,
                modelName = projectName,
                status = RunStatus.COMPLETED.name,
                timestamp = Instant.now()
            )
            eventStore.save(event)
            notifyWebSocketOutbound.notify(event)

            trainModelUseCase.execute(runId, projectName, projectConfig)
        }.catch { exception ->
            updateRunUseCase.updateStatus(runId, StageType.PREPARE, RunStatus.FAILED)

            val event = DataPreparationFailedEvent(
                runId = runId,
                modelName = projectName,
                error = exception.message ?: "Unknown error",
                timestamp = Instant.now()
            )
            eventStore.save(event)
            notifyWebSocketOutbound.notify(event)
        }
    }
}