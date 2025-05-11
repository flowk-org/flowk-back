package com.example.flowkback.domain.event.prep

import com.example.flowkback.domain.event.Event
import java.time.Instant

data class DataPreparationFailedEvent(
    val modelName: String,
    val error: String,
    val timestamp: Instant
) : Event(modelName, timestamp)
