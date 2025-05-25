package com.example.flowkback.domain.run

import java.time.LocalDateTime

data class Stage(
    val type: StageType,
    var status: RunStatus,
    var startedAt: LocalDateTime? = null,
    var finishedAt: LocalDateTime? = null,
    val logs: String = ""
)