package com.example.flowkback.domain.run

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "runs")
data class Run(
    @Id val id: String? = null,
    val projectId: String,
    val runNumber: Int,
    val status: RunStatus,
    val stages: List<Stage>,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
)





