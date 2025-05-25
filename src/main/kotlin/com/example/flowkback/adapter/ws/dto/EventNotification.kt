package com.example.flowkback.adapter.ws.dto

import java.time.LocalDateTime

data class EventNotification(
//    val projectName: String,
    val runId: String,
    val eventType: String,
    val timestamp: LocalDateTime
)