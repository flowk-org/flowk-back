package com.example.flowkback.app.api.test

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