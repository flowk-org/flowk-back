package com.example.flowkback.app.impl


import com.example.flowkback.adapter.rest.run.ReplayOfRunDto
import com.example.flowkback.adapter.rest.run.RunDto
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RunWebSocketService(private val messagingTemplate: SimpMessagingTemplate) {

    fun sendRunUpdate(runUpdate: RunDto = defaultRunUpdate()) {
        messagingTemplate.convertAndSend("/topic/app", runUpdate)
    }

    private fun defaultRunUpdate(): RunDto {
        return RunDto(
            id = 4,
            date = Instant.now().toString(),
            status = "Running",
            stages = listOf("blank", "blank", "completed", "running"),
            replayOfRun = ReplayOfRunDto(id = 2, stage = 3)
        )
    }
}