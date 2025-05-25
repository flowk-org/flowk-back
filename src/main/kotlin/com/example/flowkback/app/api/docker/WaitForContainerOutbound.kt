package com.example.flowkback.app.api.docker

interface WaitForContainerOutbound {
    fun wait(containerId: String): Int
}