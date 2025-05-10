package com.example.flowkback.app.api.pipeline

import java.io.File

interface DeployModelInbound {
    fun execute(
        servingScript: File,
        projectName: String,
        modelInputPath: String,
        pythonVersion: String
    )
}