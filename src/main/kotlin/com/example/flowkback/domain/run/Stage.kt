package com.example.flowkback.domain.run

import java.time.LocalDateTime

data class Stage(
    val type: StageType,
    val status: RunStatus,
    val startedAt: LocalDateTime? = null,
    val finishedAt: LocalDateTime? = null,
    val logs: String = ""
)