package com.example.flowkback.domain.project

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

data class Config(
    val stages: List<StageConfig>,
    val env: Env,
    val version: String? = null
)