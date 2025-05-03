package com.example.flowkback.app.api.project

import com.example.flowkback.domain.Project

interface CreateProjectInbound {
    fun execute(project: Project)
}