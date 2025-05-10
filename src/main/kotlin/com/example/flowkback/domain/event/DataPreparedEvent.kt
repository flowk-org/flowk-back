package com.example.flowkback.domain.event

import java.time.Instant

data class DataPreparedEvent(
    val status: String, // Например: "SUCCESS"
    val logs: String,
    val timestamp: Instant
) : Event(timestamp)
