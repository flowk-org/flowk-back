package com.example.flowkback.app.api.pipeline

import com.example.flowkback.domain.project.Config

interface PrepareDataInbound {
   fun execute(projectName: String, projectConfig: Config)
}