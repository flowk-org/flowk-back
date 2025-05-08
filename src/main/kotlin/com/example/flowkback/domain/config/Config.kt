package com.example.flowkback.domain.config

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "configs")
data class Config(
    @Id val id: String? = null,
    val stages: List<ConfigStage>,
    val env: Env,
    val version: String? = null
)