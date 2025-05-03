package com.example.flowkback.adapter.rest

import java.time.Instant

data class CreateProjectRequest(
    val name: String,
    val gitUrl: String
)

data class ConfigureProjectRequest(
    val pythonVersion: String,
    val requirementsPath: String,
    val entryPoint: String
)

data class ProjectResponse(
    val id: String,
    val name: String,
    val createdAt: Instant
)
