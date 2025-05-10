package com.example.flowkback.adapter.clickhouse

import java.time.LocalDateTime
import java.util.*

data class EventRecord(
    val eventId: UUID,
    val eventType: String,
    val payload: String,
    val timestamp: LocalDateTime
)