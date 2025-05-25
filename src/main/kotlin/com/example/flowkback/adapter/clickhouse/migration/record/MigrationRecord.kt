package com.example.flowkback.adapter.clickhouse.migration.record

import java.time.LocalDateTime


data class MigrationRecord(
    private val name: String,
    private val appliedAt: LocalDateTime? = null
)
