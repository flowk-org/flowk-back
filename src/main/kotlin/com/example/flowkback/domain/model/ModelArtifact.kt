package com.example.flowkback.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document("models")
data class ModelArtifact(
    @Id val id: String? = null,
    val projectId: String,
    val runId: String,
    val version: String,
    val path: String,
    val metrics: List<Metric> = listOf(),
    val features: List<Feature> = listOf(),
    val tags: List<String> = listOf(),
    val description: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)