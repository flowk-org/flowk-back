package com.example.flowkback.domain.event.test

import com.example.flowkback.domain.event.Event
import java.time.Instant

data class ModelEvaluationFailedEvent(
    val modelName: String,
    val error: String,
    val timestamp: Instant = Instant.now()
) : Event(modelName, timestamp)