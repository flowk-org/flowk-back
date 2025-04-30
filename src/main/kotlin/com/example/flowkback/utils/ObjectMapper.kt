package com.example.flowkback.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

object ObjectMapper {
    val objectMapper: ObjectMapper = ObjectMapper().registerModule(JavaTimeModule())
}