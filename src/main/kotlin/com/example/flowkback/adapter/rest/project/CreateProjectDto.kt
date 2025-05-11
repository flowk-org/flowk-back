package com.example.flowkback.adapter.rest.project

import com.example.flowkback.domain.project.Config

data class CreateProjectDto(
    val name: String,
    val gitUrl: String,
    val config: Config
)