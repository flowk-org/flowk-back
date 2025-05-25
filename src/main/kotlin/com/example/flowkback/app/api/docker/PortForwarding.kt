package com.example.flowkback.app.api.docker

/**
 * Проброс портов
 */
data class PortForwarding(val hostPort: Int, val containerPort: Int)