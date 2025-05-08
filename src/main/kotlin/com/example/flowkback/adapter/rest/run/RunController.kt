package com.example.flowkback.adapter.rest.run

import com.example.flowkback.adapter.mongo.project.ProjectRepository
import com.example.flowkback.adapter.mongo.run.RunRepository
import com.example.flowkback.domain.run.Run
import com.example.flowkback.domain.run.RunStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/runs")
class RunController(
    private val runRepository: RunRepository,
    private val projectRepository: ProjectRepository
) {

    @PostMapping
    fun startRun(@RequestBody request: StartRunRequest): ResponseEntity<Run> {
        val project = projectRepository.findByName(request.projectName)
            ?: return ResponseEntity.badRequest().build()

        val run = Run(
            projectId = project.id!!,
            configId = project.configId,
            status = RunStatus.PENDING,
            stages = listOf(),
        )

        val savedRun = runRepository.save(run)
        return ResponseEntity.ok(savedRun)
    }

    @GetMapping("/project/{projectId}")
    fun getRunsForProject(@PathVariable projectId: String): ResponseEntity<List<Run>> {
        val runs = runRepository.findAllByProjectId(projectId)
        return ResponseEntity.ok(runs)
    }

    @GetMapping("/{runId}")
    fun getRun(@PathVariable runId: String): ResponseEntity<Run> {
        val run = runRepository.findById(runId)
        return if (run.isPresent) ResponseEntity.ok(run.get())
        else ResponseEntity.notFound().build()
    }
}
