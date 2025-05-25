package com.example.flowkback.adapter.clickhouse.event

import com.clickhouse.client.api.Client
import com.example.flowkback.adapter.clickhouse.event.record.EventMapper
import com.example.flowkback.adapter.clickhouse.event.record.EventRecord
import com.example.flowkback.app.api.event.SaveEventOutbound
import com.example.flowkback.domain.event.Event
import com.example.flowkback.fw.clickhouse.ClickhouseInitializer.Companion.EVENT_TABLE
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class SaveEventAdapter(
    private val clickhouseClient: Client,
    private val eventMapper: EventMapper
) : SaveEventOutbound {
    override fun save(event: Event) {
        clickhouseClient.insert(EVENT_TABLE, listOf(eventMapper.mapEventToRecord(event)))
            .thenApply {
                println("Event saved ${event.eventType()}")
            }
    }
}