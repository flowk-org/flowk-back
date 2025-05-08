package com.example.flowkback.domain.event

import java.time.Instant

data class ModelTrainingFailedEvent(
    val modelName: String,
    val error: String,
    val timestamp: Instant
) : Event(timestamp)