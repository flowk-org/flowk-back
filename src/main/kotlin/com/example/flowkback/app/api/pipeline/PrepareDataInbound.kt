package com.example.flowkback.app.api.pipeline

interface PrepareDataInbound {
   fun execute(projectName: String, migrationsPath: String)
}