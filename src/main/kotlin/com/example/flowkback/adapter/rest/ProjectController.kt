package com.example.flowkback.adapter.rest

import com.example.flowkback.app.api.project.CreateProjectInbound
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

//@RestController
//@RequestMapping("/api/projects")
//class ProjectController(
//    private val createProjectInbound: CreateProjectInbound
//) {
//    @PostMapping
//    fun createProject(@RequestBody request: CreateProjectRequest): ResponseEntity<ProjectResponse> {
//        val result = createProjectInbound.execute(request)
//        return ResponseEntity.ok(result)
//    }
//}