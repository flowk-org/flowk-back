package com.example.flowkback.domain.config

data class ConfigStage(
    val name: String,
    val runner: String? = null,
    val script: String,
    val model: String? = null,
    val output: Output? = null
)