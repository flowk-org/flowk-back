package com.example.flowkback.adapter.rest

import com.example.flowkback.app.impl.HandleGitWebhookUseCase
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
                handleGitWebhookInbound.execute(payload)
            }
        } else {
            println("Push to non-master branch ignored: ${payload.ref}")
        }
    }
}

