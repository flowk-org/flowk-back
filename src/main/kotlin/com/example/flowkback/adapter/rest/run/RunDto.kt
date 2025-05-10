package com.example.flowkback.adapter.rest.run

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

data class ReplayOfRunDto(
    val id: Int,
    val stage: Int
)