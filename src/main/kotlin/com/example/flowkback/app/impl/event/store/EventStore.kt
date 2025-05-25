package com.example.flowkback.app.impl.event.store

import com.example.flowkback.app.api.event.SaveEventOutbound
import com.example.flowkback.domain.event.Event
import org.springframework.stereotype.Component

@Component
class EventStore(private val saveEventOutbound: SaveEventOutbound) {
    fun save(event: Event) {
        saveEventOutbound.save(event)
    }
}