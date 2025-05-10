package com.example.flowkback.app.api.pipeline

import java.io.File

interface TrainModelInbound {
    fun execute(
        trainScript: File,
        projectName: String,
        modelOutputPath: String,
        pythonVersion: String
    )
}