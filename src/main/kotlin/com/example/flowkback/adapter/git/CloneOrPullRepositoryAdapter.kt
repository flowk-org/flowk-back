package com.example.flowkback.adapter.git

import com.example.flowkback.app.api.CloneOrPullRepositoryOutbound
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.PullResult
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File

@Component
class CloneOrPullRepositoryAdapter : CloneOrPullRepositoryOutbound {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun cloneOrPullRepo(repoUrl: String, localDir: File): Git {
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
}