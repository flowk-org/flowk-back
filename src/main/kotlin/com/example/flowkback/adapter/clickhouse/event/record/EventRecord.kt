package com.example.flowkback.adapter.clickhouse.event.record

import java.time.LocalDateTime
import java.util.*

data class EventRecord(
    val eventId: UUID,
    val runId: String,
    val eventType: String,
    val payload: String,
    val timestamp: LocalDateTime
)