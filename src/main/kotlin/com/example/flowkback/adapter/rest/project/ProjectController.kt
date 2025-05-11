package com.example.flowkback.adapter.rest.project

import com.example.flowkback.adapter.mongo.project.ProjectRepository
import com.example.flowkback.app.api.project.RunProjectInbound
import com.example.flowkback.domain.project.Project
import com.example.flowkback.utils.ConfigParser
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/projects")
class ProjectController(
    private val projectRepository: ProjectRepository,
    private val runProjectInbound: RunProjectInbound
) {

    @PostMapping
    fun createProject(@RequestBody request: CreateProjectDto): ResponseEntity<Project> {
        val project = Project(
            name = request.name,
            gitUrl = request.gitUrl,
            config = ConfigParser.parseFromFile("./repos/flowk-test/mlci.yaml")
        )

        val savedProject = projectRepository.save(project)
        return ResponseEntity.ok(savedProject)
    }

    @GetMapping("/{projectName}")
    fun getProject(@PathVariable projectName: String): ResponseEntity<Project> {
        val project = projectRepository.findByName(projectName)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(project)
    }


    @PostMapping("/run")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun runProject(@RequestParam("name") projectName: String) {
        runProjectInbound.execute(projectName)
    }
}
