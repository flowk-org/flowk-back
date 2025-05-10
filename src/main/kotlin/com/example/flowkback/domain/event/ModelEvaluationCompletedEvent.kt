package com.example.flowkback.domain.event

import com.example.flowkback.domain.event.Event
import java.time.Instant

data class ModelEvaluationCompletedEvent(
    val modelName: String,
    val metrics: String,
    val logs: String,
    val evaluatedAt: Instant = Instant.now()
) : Event(evaluatedAt)