package com.example.flowkback.domain.event

import java.time.Instant

data class DataPreparationFailedEvent(
    val error: String,
    val timestamp: Instant
) : Event(timestamp)
