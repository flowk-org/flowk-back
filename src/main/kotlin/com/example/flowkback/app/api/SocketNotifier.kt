package com.example.flowkback.app.api

import com.example.flowkback.app.impl.TrainModelUseCase

interface SocketNotifier {
    fun notifyTrainingComplete(message: TrainModelUseCase.TrainingCompleteMessage)
}