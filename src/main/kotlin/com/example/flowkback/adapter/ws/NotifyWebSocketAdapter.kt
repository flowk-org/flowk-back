package com.example.flowkback.adapter.ws

import com.example.flowkback.adapter.ws.dto.NotificationMapper
import com.example.flowkback.app.api.NotifyWebSocketOutbound
import com.example.flowkback.domain.event.Event
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class NotifyWebSocketAdapter(
    private val messagingTemplate: SimpMessagingTemplate,
    private val notificationMapper: NotificationMapper
) : NotifyWebSocketOutbound {
    override fun notify(event: Event) {
        messagingTemplate.convertAndSend("/topic/app", notificationMapper.eventToNotification(event))
    }
}


