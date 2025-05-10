package com.example.flowkback.app.api.pipeline

class DataPreparationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
