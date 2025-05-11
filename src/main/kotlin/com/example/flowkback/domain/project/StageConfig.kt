package com.example.flowkback.domain.project

data class StageConfig(
    val name: String,
    val script: String,
    val model: String? = null,
    val output: Output? = null
)