package com.example.flowkback.app.api.train

import java.io.File

interface TrainModelInbound {
    fun execute(trainScript: File, modelName: String)
}