package com.example.flowkback.fw.clickhouse

import com.clickhouse.client.api.Client
import com.example.flowkback.adapter.clickhouse.event.record.EventRecord
import com.example.flowkback.adapter.clickhouse.migration.record.MigrationRecord
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component



@Component
class ClickhouseInitializer(private val clickhouseClient: Client) {
    companion object {
        const val MIGRATION_TABLE = "migrations.migrations"
        const val EVENT_TABLE = "event_store.events_buffer"
    }

    @PostConstruct
    fun initializeEventStore() {
        clickhouseClient.execute("CREATE DATABASE IF NOT EXISTS event_store;")

        clickhouseClient.execute(
            """
            CREATE TABLE IF NOT EXISTS event_store.events
            (
                event_id      UUID,
                runId         String,
                event_type    String,
                payload       String,
                timestamp     DateTime
            ) ENGINE = MergeTree()
                  ORDER BY timestamp;
        """.trimIndent()
        )

        clickhouseClient.execute(
            """
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
        """.trimIndent()
        )

        clickhouseClient.register(EventRecord::class.java, clickhouseClient.getTableSchema(Companion.EVENT_TABLE))
    }

    @PostConstruct
    fun initializeMigrationDatabase() {
        clickhouseClient.execute("CREATE DATABASE IF NOT EXISTS migrations;")

        clickhouseClient.execute(
            """
            CREATE TABLE IF NOT EXISTS migrations.migrations (
                name String,
                applied_at DateTime DEFAULT now()
            ) ENGINE = MergeTree()
                  ORDER BY applied_at;
        """.trimIndent()
        )

        clickhouseClient.register(MigrationRecord::class.java, clickhouseClient.getTableSchema(Companion.MIGRATION_TABLE))
    }
}