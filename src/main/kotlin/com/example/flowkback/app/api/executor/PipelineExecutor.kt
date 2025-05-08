package com.example.flowkback.app.api.executor

import com.example.flowkback.domain.run.StageType

interface PipelineExecutor {
    fun schedule(projectName: String, pipelines: List<StageType>)
    fun start(projectName: String, pipelines: List<StageType>)
}