package com.example.flowkback.config

import com.clickhouse.client.api.Client
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component


@Component
class ClickhouseInitializer(private val client: Client) {
    @PostConstruct
    fun initializeEventStore() {
        client.execute("CREATE DATABASE IF NOT EXISTS event_store;")

        client.execute("""
            CREATE TABLE IF NOT EXISTS event_store.events
            (
                event_id      UUID,
                event_type    String,
                payload String,
                timestamp     DateTime
            ) ENGINE = MergeTree()
                  ORDER BY timestamp;
        """.trimIndent())

        client.execute("""
            CREATE TABLE IF NOT EXISTS event_store.events_buffer AS event_store.events
                ENGINE = Buffer(
                         event_store,
                         events,
                         16,
                         10,
                         60,
                         10000,
                         100000,
                         1000000,
                         10000000
                );
        """.trimIndent())
    }
}