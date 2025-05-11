package com.example.flowkback.app.impl

import com.example.flowkback.adapter.mongo.project.ProjectRepository
import com.example.flowkback.adapter.rest.git.hook.GitPushPayload
import com.example.flowkback.app.api.CloneOrPullRepositoryOutbound
import com.example.flowkback.app.api.handler.HandleGitWebhookInbound
import com.example.flowkback.app.impl.project.RunProjectUseCase
import com.example.flowkback.domain.project.Project
import com.example.flowkback.utils.ConfigParser
import com.example.flowkback.utils.Directories.REPOS_DIR
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

@Service
@Slf4j
class HandleGitWebhookUseCase(
    private val projectRepository: ProjectRepository,
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

//            val changedFiles = extractChangedFiles(payload)
//            val config = configRepository.getByProjectName(repoName)
            projectRepository.deleteAll()
            projectRepository.save(
                Project(
                    name = "flowk-test",
                    gitUrl = "http://localhost:3003/Diachenko/flowk-test.git",
                    config = ConfigParser.parseFromFile("./repos/flowk-test/mlci.yaml")
                )
            )

            runProjectUseCase.execute(repoName)
        } catch (e: Exception) {
            logger.error("Failed to process webhook for $repoName", e)
            throw e
        }
    }

}