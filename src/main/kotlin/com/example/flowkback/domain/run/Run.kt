package com.example.flowkback.domain.run

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "runs")
data class Run(
    @Id val id: String? = null,
    val projectId: String,
    val configId: String,
    val status: RunStatus,
    val stages: List<Stage>,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)





