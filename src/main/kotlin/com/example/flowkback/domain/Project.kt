package com.example.flowkback.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "projects")
data class Project(
    @Id val id: String? = null,
    val name: String,
    val gitUrl: String,
    val config: MlciConfig,
    val stages: List<Stage> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now()
)
