package com.example.flowkback.app.impl.handler

import com.example.flowkback.adapter.rest.git.hook.dto.GitPushPayload
import com.example.flowkback.app.api.CloneOrPullRepositoryOutbound
import com.example.flowkback.app.api.handler.HandleGitWebhookInbound
import com.example.flowkback.app.impl.project.RunProjectUseCase
import com.example.flowkback.utils.Directories.REPOS_DIR
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

@Service
@Slf4j
class HandleGitWebhookUseCase(
    private val runProjectUseCase: RunProjectUseCase,
    private val cloneOrPullRepositoryOutbound: CloneOrPullRepositoryOutbound
) : HandleGitWebhookInbound {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun handle(payload: GitPushPayload) {
        val repoName = payload.repository.name
        val repoUrl = payload.repository.cloneUrl
        val localRepoDir = File("$REPOS_DIR/$repoName")

        try {
            cloneOrPullRepositoryOutbound.cloneOrPullRepo(repoUrl, localRepoDir)
            runProjectUseCase.execute(repoName)
        } catch (e: Exception) {
            logger.error("Failed to process webhook for $repoName", e)
            throw e
        }
    }

}