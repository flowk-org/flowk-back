package com.example.flowkback.app.impl.project

import com.example.flowkback.adapter.mongo.project.ProjectRepository
import com.example.flowkback.app.api.project.RunProjectInbound
import com.example.flowkback.app.impl.pipeline.PrepareDataUseCase
import org.springframework.stereotype.Service

@Service
class RunProjectUseCase(
    private val projectRepository: ProjectRepository,
    private val prepareDataUseCase: PrepareDataUseCase
): RunProjectInbound {
    override fun execute(projectName: String) {
        val project = projectRepository.findByName(projectName)
        project?.run {
            prepareDataUseCase.execute(this.name, this.config)
        }
    }
}