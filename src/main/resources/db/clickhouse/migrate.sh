#!/bin/bash
set -e

# Environment variables with defaults
CLICKHOUSE_HOST="${CLICKHOUSE_HOST:-flowk-back-clickhouse-server-1}"
CLICKHOUSE_PORT="${CLICKHOUSE_PORT:-9000}"
CLICKHOUSE_USER="${CLICKHOUSE_USER:-default}"
CLICKHOUSE_PASSWORD="${CLICKHOUSE_PASSWORD:-password}"
CLICKHOUSE_DATABASE="${CLICKHOUSE_DATABASE:-default}"

MIGRATIONS_DIR="${1:-${MIGRATIONS_DIR:-/db/migrations}}"

echo "[INFO] Running migrations from $MIGRATIONS_DIR against $CLICKHOUSE_HOST:$CLICKHOUSE_PORT"

# Ensure migrations directory exists
if [ ! -d "$MIGRATIONS_DIR" ]; then
    echo "[ERROR] Migrations directory $MIGRATIONS_DIR does not exist"
    exit 1
fi


clickhouse-client --host="$CLICKHOUSE_HOST" --port="$CLICKHOUSE_PORT" \
    --user="$CLICKHOUSE_USER" --password="$CLICKHOUSE_PASSWORD" \
    --database="$CLICKHOUSE_DATABASE" \
    --query="CREATE DATABASE IF NOT EXISTS migrations;"

# Create migrations table if it doesn't exist
clickhouse-client --host="$CLICKHOUSE_HOST" --port="$CLICKHOUSE_PORT" \
    --user="$CLICKHOUSE_USER" --password="$CLICKHOUSE_PASSWORD" \
    --database="$CLICKHOUSE_DATABASE" \
    --query="CREATE TABLE IF NOT EXISTS migrations.migrations
             (name String, applied_at DateTime DEFAULT now())
             ENGINE = MergeTree() ORDER BY applied_at;"

# Apply migrations
for file in "$MIGRATIONS_DIR"/*.sql; do
    if [ -f "$file" ]; then
        name=$(basename "$file")

        # Check if migration has already been applied
        ALREADY_APPLIED=$(clickhouse-client --host="$CLICKHOUSE_HOST" --port="$CLICKHOUSE_PORT" \
            --user="$CLICKHOUSE_USER" --password="$CLICKHOUSE_PASSWORD" \
            --database="$CLICKHOUSE_DATABASE" \
            --query="SELECT count() FROM migrations.migrations WHERE name = '$name'")

        if [ "$ALREADY_APPLIED" -eq "0" ]; then
            echo "[INFO] Applying migration $name"
            clickhouse-client --host="$CLICKHOUSE_HOST" --port="$CLICKHOUSE_PORT" \
                --user="$CLICKHOUSE_USER" --password="$CLICKHOUSE_PASSWORD" \
                --database="$CLICKHOUSE_DATABASE" \
                --queries-file="$file"

            # Record the applied migration
            clickhouse-client --host="$CLICKHOUSE_HOST" --port="$CLICKHOUSE_PORT" \
                --user="$CLICKHOUSE_USER" --password="$CLICKHOUSE_PASSWORD" \
                --database="$CLICKHOUSE_DATABASE" \
                --query="INSERT INTO migrations.migrations (name) VALUES ('$name')"
        else
            echo "[SKIP] Migration $name already applied"
        fi
    else
        echo "[INFO] No migration files found in $MIGRATIONS_DIR"
        break
    fi
done

echo "[DONE] Migrations complete"