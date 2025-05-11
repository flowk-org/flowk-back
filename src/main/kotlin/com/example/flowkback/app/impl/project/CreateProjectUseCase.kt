package com.example.flowkback.app.impl.project

import com.example.flowkback.app.api.project.CreateProjectInbound
import com.example.flowkback.domain.project.Project
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CreateProjectUseCase : CreateProjectInbound {

    @Transactional
    override fun execute(project: Project) {
        if (fromScratch()) {
            validateConfig()
            saveConfig()
            createGitRepository()
            giveRepositoryToUser()
        } else {
            cloneRepository()
            validateConfig()
            saveConfig()
        }
    }

    private fun saveConfig() {
        TODO("Not yet implemented")
    }

    private fun cloneRepository() {
        TODO("Not yet implemented")
    }

    private fun giveRepositoryToUser() {
        TODO("Not yet implemented")
    }

    private fun fromScratch(): Boolean {
        return false
    }

    private fun createGitRepository() {
        TODO("Not yet implemented")
    }

    private fun validateConfig() {
        TODO("Not yet implemented")
    }
}