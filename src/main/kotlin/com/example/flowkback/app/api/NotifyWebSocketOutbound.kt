package com.example.flowkback.app.api

import com.example.flowkback.domain.event.Event


interface NotifyWebSocketOutbound {
    fun notify(event: Event)
}