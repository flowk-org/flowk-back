package com.example.flowkback.adapter.ws

import com.example.flowkback.app.api.SocketNotifier
import com.example.flowkback.app.api.pipeline.TrainingCompleteMessage
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class WebSocketNotifier(
    private val messagingTemplate: SimpMessagingTemplate
) : SocketNotifier {
    override fun notifyTrainingComplete(message: TrainingCompleteMessage) {
        messagingTemplate.convertAndSend("/topic/training-complete", message)
    }
}
