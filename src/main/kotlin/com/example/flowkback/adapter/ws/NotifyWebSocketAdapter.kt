package com.example.flowkback.adapter.ws

import com.example.flowkback.adapter.clickhouse.EventRecord
import com.example.flowkback.app.api.NotifyWebSocketOutbound
import com.example.flowkback.domain.event.Event
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Component
class NotifyWebSocketAdapter(
    private val messagingTemplate: SimpMessagingTemplate
) : NotifyWebSocketOutbound {
    override fun notify(event: Event) {
        messagingTemplate.convertAndSend("/topic/app", mapEventToRecord(event))
    }

    private fun mapEventToRecord(event: Event): EventNotification {
        return EventNotification(
            projectName = event.projectName(),
            runId = "4",
            eventType = event.eventType(),
            timestamp = LocalDateTime.from(event.timestamp().atZone(ZoneId.systemDefault()))
        )
    }
}


