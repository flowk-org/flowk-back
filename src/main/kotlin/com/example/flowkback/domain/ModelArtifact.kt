package com.example.flowkback.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "models")
data class ModelArtifact(
    @Id val id: String? = null,
    val projectId: String,
    val version: String,
    val pathInMinio: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val metrics: Map<String, Double> = emptyMap()
)

