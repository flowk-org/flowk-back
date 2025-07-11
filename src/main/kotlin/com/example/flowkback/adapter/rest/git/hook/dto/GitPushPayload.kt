package com.example.flowkback.adapter.rest.git.hook.dto

import com.fasterxml.jackson.annotation.JsonAlias

data class GitPushPayload(
    val ref: String,
    val before: String,
    val after: String,
    val commits: List<Commit>,
    val repository: Repository,
    @JsonAlias("head_commit")
    val headCommit: Commit
) {
    data class Repository(
        val name: String,
        @JsonAlias("clone_url")
        val cloneUrl: String
    )

    data class Commit(
        val id: String,
        val message: String,
        val added: List<String>,
        val removed: List<String>,
        val modified: List<String>
    )
}
