CREATE DATABASE IF NOT EXISTS event_store;

CREATE TABLE IF NOT EXISTS event_store.events
(
    event_id      String,
    type          String,
    event_payload String,
    timestamp     DateTime
) ENGINE = MergeTree()
      ORDER BY timestamp;

CREATE TABLE IF NOT EXISTS event_store.events_buffer AS event_store.events
    ENGINE = Buffer(
             event_store,
             events,
             16,
             10,
             60,
             10000,
             100000,
             1000000,
             10000000
    );
