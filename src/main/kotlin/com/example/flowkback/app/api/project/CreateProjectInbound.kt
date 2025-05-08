package com.example.flowkback.app.api.project

import com.example.flowkback.domain.project.Project

interface CreateProjectInbound {
    fun execute(project: Project)
}