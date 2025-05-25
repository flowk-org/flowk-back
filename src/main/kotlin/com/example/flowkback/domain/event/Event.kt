package com.example.flowkback.domain.event

import com.example.flowkback.utils.ObjectMapper.objectMapper
import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Instant
import java.util.*

abstract class Event(
    private val runId: String,
    private val timestamp: Instant
) {
    fun eventId(): UUID = UUID.randomUUID()

    fun runId(): String = runId

    fun eventType(): String = this::class.simpleName ?: "UnknownEvent"

    fun payload(): String = objectMapper.writeValueAsString(this)

    fun timestamp(): Instant = timestamp
}