package com.example.flowkback.app.api

import java.io.File

interface TrainModelInbound {
    fun execute(trainScript: File, modelName: String)
}