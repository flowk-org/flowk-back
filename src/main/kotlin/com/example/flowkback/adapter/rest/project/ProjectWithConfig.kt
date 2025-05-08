package com.example.flowkback.adapter.rest.project

import com.example.flowkback.domain.config.Config
import com.example.flowkback.domain.project.Project

data class ProjectWithConfig(
    val project: Project,
    val config: Config
)
