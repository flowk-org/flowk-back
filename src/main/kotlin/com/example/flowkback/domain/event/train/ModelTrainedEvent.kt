package com.example.flowkback.domain.event.train

import com.example.flowkback.domain.event.Event
import java.time.Instant

data class ModelTrainedEvent(
    val modelName: String,
    val modelUrl: String,
    val trainedAt: Instant = Instant.now(),
) : Event(modelName, trainedAt)

