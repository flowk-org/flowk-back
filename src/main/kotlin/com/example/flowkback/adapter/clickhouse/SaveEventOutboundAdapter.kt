package com.example.flowkback.adapter.clickhouse

import com.clickhouse.client.api.Client
import com.example.flowkback.app.api.event.SaveEventOutbound
import com.example.flowkback.app.api.event.Event
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId

private const val EVENT_TABLE = "event_store.events_buffer"

@Component
class SaveEventOutboundAdapter(private val clickhouseClient: Client) : SaveEventOutbound {
    override fun save(event: Event) {
        clickhouseClient.register(EventRecord::class.java, clickhouseClient.getTableSchema(EVENT_TABLE))

        clickhouseClient.insert(EVENT_TABLE, listOf(mapEventToRecord(event)))
            .thenApply {
                println("Event saved ${event.eventType()}")
            }
    }

    private fun mapEventToRecord(event: Event): EventRecord {
        return EventRecord(
            event.eventId(),
            event.eventType(),
            event.payload(),
            LocalDateTime.from(event.timestamp().atZone(ZoneId.systemDefault()))
        )
    }
}