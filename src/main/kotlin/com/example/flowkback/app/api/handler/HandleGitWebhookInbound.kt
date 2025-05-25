package com.example.flowkback.app.api.handler

import com.example.flowkback.adapter.rest.git.hook.dto.GitPushPayload

interface HandleGitWebhookInbound {
    fun handle(payload: GitPushPayload)
}