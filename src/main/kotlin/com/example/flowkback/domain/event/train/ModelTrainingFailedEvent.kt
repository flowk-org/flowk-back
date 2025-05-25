package com.example.flowkback.domain.event.train

import com.example.flowkback.domain.event.Event
import java.time.Instant

data class ModelTrainingFailedEvent(
    val runId: String,
    val modelName: String,
    val error: String,
    val timestamp: Instant
) : Event(runId, timestamp)