package com.example.flowkback.domain.event.test

import com.example.flowkback.domain.event.Event
import java.time.Instant

data class ModelEvaluationStartedEvent(
    val runId: String,
    val modelName: String,
    val startedAt: Instant = Instant.now()
): Event(runId, startedAt)
