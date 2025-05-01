package com.example.flowkback.app.api

import com.example.flowkback.adapter.rest.GitPushPayload

interface HandleGitWebhookInbound {
    fun execute(payload: GitPushPayload)
}