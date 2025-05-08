package com.example.flowkback.app.api.docker

import kotlinx.coroutines.flow.Flow

interface StreamLogsOutbound {
    fun stream(containerId: String): Flow<String>
}