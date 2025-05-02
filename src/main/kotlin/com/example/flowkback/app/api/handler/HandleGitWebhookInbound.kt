package com.example.flowkback.app.api.handler

import com.example.flowkback.adapter.rest.GitPushPayload

interface HandleGitWebhookInbound {
    fun execute(payload: GitPushPayload)
}