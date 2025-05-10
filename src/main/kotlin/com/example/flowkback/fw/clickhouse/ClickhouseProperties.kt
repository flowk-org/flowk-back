package com.example.flowkback.fw.clickhouse

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "clickhouse")
data class ClickhouseProperties(
    var host: String = "localhost",
    var port: Int = 9000,
    var user: String = "default",
    var password: String? = null,
)