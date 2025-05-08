package com.example.flowkback.app.api.test

import com.example.flowkback.domain.event.Event
import java.time.Instant

data class ModelEvaluationStartedEvent(
    val modelName: String,
    val startedAt: Instant = Instant.now()
)

data class ModelEvaluationCompletedEvent(
    val modelName: String,
    val metrics: String,
    val logs: String,
    val evaluatedAt: Instant = Instant.now()
) : Event(evaluatedAt)

data class ModelEvaluationFailedEvent(
    val modelName: String,
    val error: String,
    val timestamp: Instant = Instant.now()
) : Event(timestamp)
