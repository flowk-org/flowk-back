package com.example.flowkback.app.impl.project

import com.example.flowkback.adapter.mongo.project.ProjectRepository
import com.example.flowkback.adapter.mongo.run.RunRepository
import com.example.flowkback.app.api.project.RunProjectInbound
import com.example.flowkback.app.impl.pipeline.prepare.PrepareDataUseCase
import com.example.flowkback.domain.run.Run
import com.example.flowkback.domain.run.RunStatus
import com.example.flowkback.domain.run.Stage
import com.example.flowkback.domain.run.StageType
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class RunProjectUseCase(
    private val projectRepository: ProjectRepository,
    private val prepareDataUseCase: PrepareDataUseCase,
    private val runRepository: RunRepository
) : RunProjectInbound {
    override fun execute(projectName: String) {
        val project = projectRepository.findByName(projectName)
        val runId = project?.id?.let { createDefaultRun(it) }?.id
        project?.run {
            runId?.let { prepareDataUseCase.execute(it, this.name, this.config) }
        }
    }

    private fun createDefaultRun(projectId: String): Run? {
        val runNumber = runRepository.findTopByProjectIdOrderByRunNumberDesc(projectId)?.runNumber?.plus(1)

        return runNumber?.let {
            Run(
                projectId = projectId,
                runNumber = it,
                status = RunStatus.RUNNING,
                stages = mutableListOf(
                    Stage(StageType.PREPARE, RunStatus.PENDING),
                    Stage(StageType.TRAIN, RunStatus.PENDING),
                    Stage(StageType.TEST, RunStatus.PENDING),
                    Stage(StageType.DEPLOY, RunStatus.PENDING),
                ),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ).run {
                runRepository.save(this)
            }
        }
    }
}