package com.example.flowkback.app.api.pipeline

data class PreparationCompleteMessage(
    val status: String, // "SUCCESS" или "FAILED"
    val message: String
)
