package com.example.flowkback.adapter.rest.project

import com.example.flowkback.adapter.mongo.project.ProjectRepository
import com.example.flowkback.app.api.project.ConfigRepository
import com.example.flowkback.domain.project.Project
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/projects")
class ProjectController(
    private val projectRepository: ProjectRepository,
    private val projectConfigRepository: ConfigRepository
) {

    @PostMapping
    fun createProject(@RequestBody request: CreateProjectDto): ResponseEntity<Project> {
        val savedConfig = projectConfigRepository.save(request.config)

        val project = Project(
            name = request.name,
            gitUrl = request.gitUrl,
            configId = savedConfig.id!!
        )

        val savedProject = projectRepository.save(project)
        return ResponseEntity.ok(savedProject)
    }

    @GetMapping("/{projectName}")
    fun getProject(@PathVariable projectName: String): ResponseEntity<ProjectWithConfig> {
        val project = projectRepository.findByName(projectName)
            ?: return ResponseEntity.notFound().build()

        val config = projectConfigRepository.getById(project.configId)

        return ResponseEntity.ok(config?.let { ProjectWithConfig(project, it) })
    }
}
