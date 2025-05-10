package com.example.flowkback.app.api

interface ApplyMigrationsOutbound {
    fun apply(migrationsDirPath: String = "/migrations")
}