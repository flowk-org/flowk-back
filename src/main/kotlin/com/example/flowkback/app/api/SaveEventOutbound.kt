package com.example.flowkback.app.api

import com.example.flowkback.domain.event.Event


interface SaveEventOutbound {
    fun save(event: Event)
}