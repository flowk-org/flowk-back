package com.example.flowkback.app.api.event

import java.time.Instant

data class ModelTrainingFailedEvent(
        val modelName: String,
        val error: String,
        val timestamp: Instant
    ): Event(timestamp)