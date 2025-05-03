package com.example.flowkback.app.api.train

data class TrainingCompleteMessage(
        val modelName: String,
        val status: String,
        val modelUrl: String
    )