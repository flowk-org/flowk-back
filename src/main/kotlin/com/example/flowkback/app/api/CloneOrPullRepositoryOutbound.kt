package com.example.flowkback.app.api

import org.eclipse.jgit.api.Git
import java.io.File


interface CloneOrPullRepositoryOutbound {
    fun cloneOrPullRepo(repoUrl: String, localDir: File): Git
}