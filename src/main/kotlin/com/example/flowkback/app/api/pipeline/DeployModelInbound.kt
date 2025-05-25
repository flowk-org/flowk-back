package com.example.flowkback.app.api.pipeline

import com.example.flowkback.domain.project.Config

interface DeployModelInbound {
    fun execute(runId: String, projectName: String, projectConfig: Config)
}