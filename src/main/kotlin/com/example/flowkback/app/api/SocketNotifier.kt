package com.example.flowkback.app.api

import com.example.flowkback.app.api.train.TrainingCompleteMessage


interface SocketNotifier {
    fun notifyTrainingComplete(message: TrainingCompleteMessage)
}