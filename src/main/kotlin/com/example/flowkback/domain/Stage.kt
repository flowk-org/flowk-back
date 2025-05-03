package com.example.flowkback.domain

import java.time.LocalDateTime

data class Stage(
    val name: String,
    val status: StageStatus = StageStatus.PENDING,
    val startedAt: LocalDateTime? = null,
    val finishedAt: LocalDateTime? = null,
    val logs: String? = null
)