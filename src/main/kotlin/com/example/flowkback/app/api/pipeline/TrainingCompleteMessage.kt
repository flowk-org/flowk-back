package com.example.flowkback.app.api.pipeline

data class TrainingCompleteMessage(
        val modelName: String,
        val status: String,
        val modelUrl: String
    )