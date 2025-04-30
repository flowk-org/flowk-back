package com.example.flowkback.domain.event

import java.time.Instant

data class ModelTrainedEvent(
    val modelName: String,
    val modelUrl: String,
    val logs: String,
    val trainedAt: Instant = Instant.now(),
) : Event(trainedAt)



