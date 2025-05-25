package com.example.flowkback.domain.event.deploy

import com.example.flowkback.domain.event.Event
import java.time.Instant

data class ModelDeploymentCompletedEvent(
    val runId: String,
    val modelName: String,
//    val logs: String,
    val timestamp: Instant = Instant.now()
) : Event(runId, timestamp)