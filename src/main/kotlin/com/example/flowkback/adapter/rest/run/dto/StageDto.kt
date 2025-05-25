package com.example.flowkback.adapter.rest.run.dto

data class StageDto(
    val type: String,
    val status: String,
    val startedAt: String?,
    val finishedAt: String?
)