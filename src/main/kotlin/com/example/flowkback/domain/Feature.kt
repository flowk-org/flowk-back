package com.example.flowkback.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "features")
data class Feature(
    @Id val id: String? = null,
    val projectId: String,
    val name: String,
    val sqlSnippet: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
