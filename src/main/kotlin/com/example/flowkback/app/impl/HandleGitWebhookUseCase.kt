package com.example.flowkback.app.impl

import com.example.flowkback.adapter.rest.GitPushPayload
import com.example.flowkback.app.api.handler.HandleGitWebhookInbound
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.PullResult
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.springframework.stereotype.Service
import java.io.File

@Service
class HandleGitWebhookUseCase(
    private val trainModelUseCase: TrainModelUseCase
) : HandleGitWebhookInbound {
    override fun execute(payload: GitPushPayload) {
        val repoUrl = payload.repository.cloneUrl
        val modelName = payload.repository.name
        val localRepoDir = File("repos/${modelName}")

        val credentials = UsernamePasswordCredentialsProvider("Diachenko", "Yurok22!")

        val git = if (!localRepoDir.exists()) {
            Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(localRepoDir)
                .setCredentialsProvider(credentials)
                .call()
        } else {
            val existingGit = Git.open(localRepoDir)
            val pullResult: PullResult = existingGit.pull()
                .setCredentialsProvider(credentials)
                .call()

            if (!pullResult.isSuccessful) {
                throw IllegalStateException("Failed to pull latest changes for $modelName")
            }

            existingGit
        }

        val trainScript = File(localRepoDir, "train.py")
        if (!trainScript.exists()) {
            throw IllegalStateException("train.py not found in $modelName repository")
        }

        trainModelUseCase.execute(trainScript, modelName)
    }

    private fun exec() {
        parseChangedFiles()
        checkConfigurationWithTheseFiles()
        scheduleExecution()
        startNeededUseCases()
    }

    private fun scheduleExecution() {
        TODO("Not yet implemented")
    }

    private fun startNeededUseCases() {
        TODO("Not yet implemented")
    }

    private fun checkConfigurationWithTheseFiles() {
        TODO("Not yet implemented")
    }

    private fun parseChangedFiles() {
        TODO("Not yet implemented")
    }
}
