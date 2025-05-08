package com.example.flowkback.app.api.handler

import com.example.flowkback.adapter.rest.git.hook.GitPushPayload

interface HandleGitWebhookInbound {
    fun handle(payload: GitPushPayload)
}