package com.example.flowkback.app.api

import com.example.flowkback.app.api.pipeline.TrainingCompleteMessage


interface SocketNotifier {
    fun notifyTrainingComplete(message: TrainingCompleteMessage)
}