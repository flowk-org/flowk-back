package com.example.flowkback.domain.event.test

import com.example.flowkback.domain.event.Event
import java.time.Instant

data class ModelEvaluationCompletedEvent(
    val runId: String,
    val modelName: String,
    val metrics: String,
    val evaluatedAt: Instant = Instant.now()
) : Event(runId, evaluatedAt)