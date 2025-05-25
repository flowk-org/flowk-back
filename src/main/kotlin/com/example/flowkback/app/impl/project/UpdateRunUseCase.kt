package com.example.flowkback.app.impl.project

import com.example.flowkback.adapter.mongo.run.RunRepository
import com.example.flowkback.domain.run.RunStatus
import com.example.flowkback.domain.run.StageType
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UpdateRunUseCase(
    private val runRepository: RunRepository
) {
    fun updateStatus(runId: String, stageType: StageType, status: RunStatus) {
        runRepository.findById(runId).get().let {
            val stage = it.stages[stageType.index]
            when (status) {
                RunStatus.PENDING -> {}
                RunStatus.RUNNING -> stage.startedAt = LocalDateTime.now()
                RunStatus.COMPLETED -> stage.finishedAt = LocalDateTime.now()
                RunStatus.FAILED -> stage.finishedAt = LocalDateTime.now()
            }
            stage.status = status
            it.updatedAt = LocalDateTime.now()
            runRepository.save(it)
        }
    }
}