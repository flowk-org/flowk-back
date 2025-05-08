package com.example.flowkback.domain.project

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "projects")
data class Project(
    @Id val id: String? = null,
    @Indexed(unique = true) val name: String,
    val gitUrl: String,
    val configId: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)