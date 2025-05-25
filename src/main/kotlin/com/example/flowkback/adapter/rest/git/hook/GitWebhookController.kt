package com.example.flowkback.adapter.rest.git.hook

import com.example.flowkback.adapter.rest.git.hook.dto.GitPushPayload
import com.example.flowkback.app.impl.handler.HandleGitWebhookUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RequestMapping("/git")
@RestController
class GitWebhookController(
    private val handleGitWebhookInbound: HandleGitWebhookUseCase,
) {
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    @PostMapping("/webhook")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun handleWebhook(@RequestBody payload: GitPushPayload) {
        if (payload.ref == "refs/heads/master") {
            coroutineScope.launch {
                handleGitWebhookInbound.handle(payload)
            }
        } else {
            println("Push to non-master branch ignored: ${payload.ref}")
        }
    }
}

