package com.example.flowkback.domain.event.deploy

import com.example.flowkback.domain.event.Event
import java.time.Instant

data class ModelDeploymentFailedEvent(
    val modelName: String,
    val error: String,
    val timestamp: Instant = Instant.now()
) : Event(modelName, timestamp)