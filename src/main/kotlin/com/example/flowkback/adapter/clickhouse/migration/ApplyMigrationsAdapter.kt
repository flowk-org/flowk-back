package com.example.flowkback.adapter.clickhouse.migration

import com.clickhouse.client.api.Client
import com.clickhouse.client.api.Client.newBinaryFormatReader
import com.clickhouse.client.api.query.QuerySettings
import com.example.flowkback.adapter.clickhouse.migration.record.MigrationRecord
import com.example.flowkback.app.api.ApplyMigrationsOutbound
import com.example.flowkback.fw.clickhouse.ClickhouseInitializer.Companion.MIGRATION_TABLE
import org.springframework.stereotype.Component
import java.io.File
import java.math.BigInteger
import java.time.Instant

@Component
class ApplyMigrationsAdapter(private val clickhouseClient: Client) : ApplyMigrationsOutbound {
    override fun apply(migrationsDirPath: String) {
        clickhouseClient.use { clickhouseClient ->
            val migrationsDir = File(migrationsDirPath)
            if (!migrationsDir.exists() || !migrationsDir.isDirectory) {
                throw IllegalArgumentException("Invalid migrations directory: $migrationsDirPath")
            }

            val migrationFiles = migrationsDir.listFiles { f -> f.extension == "sql" }
                ?.sortedBy { it.name }
                ?: emptyList()

            val appliedMigrations = mutableListOf<String>()
            for (file in migrationFiles) {
                val name = file.name
                if (isNotAlreadyApplied(name)) {
                    println("[INFO] Applying migration $name")

                    clickhouseClient.execute(file.readText())
                    appliedMigrations.add(name)
                } else {
                    println("[SKIP] Migration $name already applied")
                }
            }
            markAsApplied(appliedMigrations)

            println("[DONE] Migrations complete at ${Instant.now()}")
        }
    }

    private fun isNotAlreadyApplied(name: String): Boolean {
        val query = "SELECT count() AS count FROM migrations.migrations WHERE name = '$name'"

        return clickhouseClient.query(query, QuerySettings()).get().use { response ->
            val reader = newBinaryFormatReader(response)
            if (reader.hasNext()) {
                val row = reader.next()
                val count = when (val value = row.getValue("count")) {
                    is BigInteger -> value.toLong()
                    is Number -> value.toLong()
                    else -> throw IllegalStateException("Unexpected type for count: ${value?.javaClass}")
                }
                count == 0L
            } else {
                true
            }
        }
    }

    private fun markAsApplied(migrations: List<String>) {
        clickhouseClient.insert(MIGRATION_TABLE, migrations.map { MigrationRecord(it) })
    }
}
