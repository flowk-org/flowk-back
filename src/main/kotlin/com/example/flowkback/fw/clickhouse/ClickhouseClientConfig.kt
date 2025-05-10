package com.example.flowkback.fw.clickhouse

import com.clickhouse.client.api.Client
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ClickhouseClientConfig {
    @Bean
    fun client(): Client {
        return Client.Builder()
            .addEndpoint("http://localhost:8123")
            .setUsername("default")
            .setPassword("password")
            .build()
    }
}