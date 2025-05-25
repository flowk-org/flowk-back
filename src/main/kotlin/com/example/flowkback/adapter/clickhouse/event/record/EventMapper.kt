package com.example.flowkback.adapter.clickhouse.event.record

import com.example.flowkback.domain.event.Event
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class EventMapper {
    fun mapEventToRecord(event: Event): EventRecord {
        return with(event) {
            EventRecord(
                eventId = eventId(),
                runId = runId(),
                eventType = eventType(),
                payload = payload(),
                timestamp = LocalDateTime.from(event.timestamp().atZone(ZoneId.systemDefault()))
            )
        }
    }
}