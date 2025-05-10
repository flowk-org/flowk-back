package com.example.flowkback.adapter.clickhouse

import java.time.LocalDateTime


data class MigrationRecord(
    private val name: String,
    private val appliedAt: LocalDateTime? = null
)
