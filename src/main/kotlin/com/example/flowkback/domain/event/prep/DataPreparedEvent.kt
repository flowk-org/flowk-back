package com.example.flowkback.domain.event.prep

import com.example.flowkback.domain.event.Event
import java.time.Instant

data class DataPreparedEvent(
    val modelName: String,
    val status: String, // Например: "SUCCESS"
//    val logs: String,
    val timestamp: Instant
) : Event(modelName, timestamp)
