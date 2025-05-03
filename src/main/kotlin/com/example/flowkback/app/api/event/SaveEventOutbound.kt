package com.example.flowkback.app.api.event


interface SaveEventOutbound {
    fun save(event: Event)
}