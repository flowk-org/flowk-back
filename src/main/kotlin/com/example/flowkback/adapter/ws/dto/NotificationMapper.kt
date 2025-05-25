package com.example.flowkback.adapter.ws.dto

import com.example.flowkback.domain.event.Event
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class NotificationMapper {
    fun eventToNotification(event: Event): EventNotification {
        return EventNotification(
            runId = event.runId(),
            eventType = event.eventType(),
            timestamp = LocalDateTime.from(event.timestamp().atZone(ZoneId.systemDefault()))
        )
    }
}