package com.example.flowkback.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "metrics")
data class Metric(
    @Id val id: String? = null,
    val modelId: String,
    val name: String,
    val value: Double
)
