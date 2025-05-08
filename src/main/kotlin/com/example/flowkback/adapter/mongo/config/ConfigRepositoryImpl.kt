package com.example.flowkback.adapter.mongo.config

import com.example.flowkback.adapter.mongo.project.ProjectRepository
import com.example.flowkback.app.api.project.ConfigRepository
import com.example.flowkback.domain.config.*
import com.example.flowkback.domain.project.Project
import com.example.flowkback.utils.ConfigParser
import org.springframework.stereotype.Repository

@Repository
class ConfigRepositoryImpl(
    private val configMongoRepository: ConfigMongoRepository,
    private val projectRepository: ProjectRepository
) : ConfigRepository {

    override fun save(config: Config): Config {
        return configMongoRepository.save(config)
    }

    override fun delete(id: String) {
        configMongoRepository.deleteById(id)
    }

    override fun getByProjectName(projectName: String): Config {
        projectRepository.deleteAll()
        configMongoRepository.deleteAll()

        val config = configMongoRepository.save(
            ConfigParser.parseFromFile("./repos/flowk-test/mlci.yaml")
        )

        projectRepository.save(
            Project(
                name = "flowk-test",
                gitUrl = "http://localhost:3003/Diachenko/flowk-test.git",
                configId = config.id!!
            )
        )

        val project = projectRepository.findByName(projectName)
            ?: throw IllegalArgumentException("Project not found: $projectName")

        return configMongoRepository.findById(project.configId)
            .orElseThrow { IllegalStateException("Config not found: ${project.configId}") }
    }


    override fun getById(id: String): Config? {
        return configMongoRepository.findById(id).orElse(null)
    }
}
