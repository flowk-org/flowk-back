package com.example.flowkback.adapter.rest.run.dto

import com.example.flowkback.adapter.rest.run.dto.ReplayOfRunDto
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RunDto(
    val id: Int,
    val date: String,
    val status: String,
    val stages: List<String>,
    val replayOfRun: ReplayOfRunDto? = null,
    val replayedFromStages: List<Int>? = null
)