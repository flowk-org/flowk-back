package com.example.flowkback.app.api.project

import com.example.flowkback.domain.config.Config


interface ConfigRepository {
    fun save(config: Config): Config
    fun delete(id: String)
    fun getByProjectName(projectName: String): Config
    fun getById(id: String): Config?
}
