package com.example.flowkback.app.impl

import com.example.flowkback.adapter.rest.git.hook.GitPushPayload
import com.example.flowkback.app.api.executor.PipelineExecutor
import com.example.flowkback.app.api.handler.HandleGitWebhookInbound
import com.example.flowkback.app.api.project.ConfigRepository
import com.example.flowkback.domain.config.Config
import com.example.flowkback.domain.run.StageType
import com.example.flowkback.utils.ConfigParser
import lombok.extern.slf4j.Slf4j
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.PullResult
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

@Service
@Slf4j
class HandleGitWebhookUseCase(
    private val configRepository: ConfigRepository,
    private val pipelineExecutor: PipelineExecutor
) : HandleGitWebhookInbound {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun handle(payload: GitPushPayload) {
        val repoName = payload.repository.name
        val repoUrl = payload.repository.cloneUrl
        val localRepoDir = File("repos/$repoName")

        try {
            cloneOrPullRepo(repoUrl, localRepoDir)

            val changedFiles = extractChangedFiles(payload)
            val config = configRepository.getByProjectName(repoName)

            val pipelinesToRun = analyzePipelineTriggers(config, changedFiles)
            pipelineExecutor.schedule(repoName, pipelinesToRun)
            pipelineExecutor.start(repoName, pipelinesToRun)
        } catch (e: Exception) {
            logger.error("Failed to process webhook for $repoName", e)
            throw e
        }
    }

    private fun cloneOrPullRepo(repoUrl: String, localDir: File): Git {
        val credentials = UsernamePasswordCredentialsProvider("Diachenko", "Yurok22!")

        return if (!localDir.exists()) {
            logger.info("Cloning repository from $repoUrl into ${localDir.path}")
            Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(localDir)
                .setCredentialsProvider(credentials)
                .call()
        } else {
            logger.info("Pulling latest changes for ${localDir.name}")
            val git = Git.open(localDir)
            val pullResult: PullResult = git.pull()
                .setCredentialsProvider(credentials)
                .call()
            if (!pullResult.isSuccessful) {
                throw IllegalStateException("Git pull failed for ${localDir.name}")
            }
            git
        }
    }

    private fun extractChangedFiles(payload: GitPushPayload): List<String> {
        return payload.commits
            .flatMap { it.added + it.modified + it.removed }
            .distinct()
    }

    private fun analyzePipelineTriggers(config: Config, changedFiles: List<String>): List<StageType> {
        val result = mutableListOf<StageType>()
        if (changedFiles.any { it.endsWith(".py") && it.contains("train") }) {
            result.addAll(
                listOf(
                    StageType.TRAIN,
                    StageType.TEST,
                    StageType.DEPLOY
                )
            )
        } else if (changedFiles.any { it.endsWith(".py") && it.contains("test") }) {
            result.addAll(
                listOf(
                    StageType.TEST,
                    StageType.DEPLOY
                )
            )
        } else if (changedFiles.any { it.endsWith(".py") && it.contains("serving") }) {
            result.addAll(
                listOf(
                    StageType.DEPLOY
                )
            )
        } else if (changedFiles.any { it.endsWith("mlci.yaml") }) {
            result.addAll(
                listOf(
                    StageType.PREPARE,
                    StageType.TRAIN,
                    StageType.TEST,
                    StageType.DEPLOY
                )
            )
        } else if (changedFiles.any { it.contains("migrations") }) {
            result.addAll(
                listOf(
                    StageType.PREPARE,
//                    StageType.TRAIN,
//                    StageType.TEST,
//                    StageType.DEPLOY
                )
            )
        }
        return result
    }
}
