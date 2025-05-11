package com.example.flowkback.domain.project

import com.fasterxml.jackson.annotation.JsonProperty

data class Env(
    @JsonProperty("py_version")
    val pyVersion: String,
    val dependencies: String,
    val runner: String,
)
