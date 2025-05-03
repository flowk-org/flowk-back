package com.example.flowkback.app.api.event

import java.time.Instant

data class ModelTrainedEvent(
    val modelName: String,
    val modelUrl: String,
    val logs: String,
    val trainedAt: Instant = Instant.now(),
) : Event(trainedAt)



