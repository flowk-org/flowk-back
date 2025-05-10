package com.example.flowkback.app.api.pipeline

import java.io.File

interface EvaluateModelInbound {
    fun execute(
        testScript: File,
        projectName: String,
        modelInputPath: String,
        metricsOutputPath: String,
        pythonVersion: String
    )
}